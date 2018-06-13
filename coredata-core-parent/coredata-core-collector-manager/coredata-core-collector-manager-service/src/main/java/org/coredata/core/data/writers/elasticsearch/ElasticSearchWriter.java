package org.coredata.core.data.writers.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Record;
import org.coredata.core.data.Writer;
import org.coredata.core.data.exception.DataException;
import org.coredata.core.data.vo.TableMeta;
import org.coredata.core.data.writers.WriterProperties;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service(value = "esWriter")
@Scope("prototype")
public class ElasticSearchWriter extends Writer {

	private Logger logger = LoggerFactory.getLogger(ElasticSearchWriter.class);

	@Value("${spring.kafka.topics.data_import}")
	private String topic;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private ElasticSaver elasticSearchService;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	private String currentIndex;

	private List<FieldDef> fields = new ArrayList<>();

	private boolean appendMode = false;

	@Override
	public void prepare(PluginConfig writerConfig, TableMeta tableMeta, String token) {

		super.prepare(writerConfig, tableMeta, token);

		String indexName = writerConfig.getString(ElasticSearchProperties.INDEX_NAME);
		String indexTemplate = writerConfig.getString(ElasticSearchProperties.INDEX_TEMPLATE);
		appendMode = writerConfig.getBoolean(WriterProperties.WRITE_MODE, true);
		try {
			//是否追加,true为追加,false删除已有数据索引
			if (!appendMode) {
				if (!StringUtils.isEmpty(indexName)) {
					elasticsearchTemplate.deleteIndex(indexName + "_*");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JsonNode properties = null;

		try (InputStream is = ElasticSearchWriter.class.getResourceAsStream("/template.json")) {
			ObjectNode template = (ObjectNode) mapper.readTree(is);
			template.put("template", indexName + "_*");
			ObjectNode propDef = (ObjectNode) template.get("mappings").get("prop_def");
			properties = mapper.readTree(indexTemplate);
			propDef.set("properties", properties);
			elasticsearchTemplate.getClient().admin().indices()
					.putTemplate(new PutIndexTemplateRequest(indexName + "_template").source(mapper.writeValueAsString(template), XContentType.JSON)).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			logger.error("Init index template error.", e);
			throw new DataException(e);
		}

		Iterator<Entry<String, JsonNode>> ite = properties.fields();
		while (ite.hasNext()) {
			Entry<String, JsonNode> entry = ite.next();
			FieldDef def = new FieldDef(entry.getKey(), entry.getValue().get("type").asText());
			fields.add(def);
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		currentIndex = indexName + "_" + format.format(new Date(System.currentTimeMillis()));
	}

	@Override
	public void execute(Record record) {

		ObjectNode document = mapper.createObjectNode();

		try {
			for (int i = 0, len = record.size(); i < len; i++) {
				String val = record.get(i).toString();
				FieldDef fd = fields.get(i);
				String name = fd.getName();
				switch (fd.getType().toLowerCase()) {
				case "date":
					document.put(name, Long.parseLong(val));
					break;
				case "long":
					document.put(name, Double.valueOf(String.valueOf(val)).longValue());
					break;
				case "integer":
					document.put(name, Double.valueOf(String.valueOf(val)).intValue());
					break;
				case "double":
					document.put(name, Double.parseDouble(val));
					break;
				case "float":
					document.put(name, Float.parseFloat(val));
					break;
				case "boolean":
					document.put(name, Boolean.parseBoolean(val));
					break;
				case "text":
				case "keyword":
				case "ip":
					document.put(name, val);
					break;
				}
			}

			String recordJson = mapper.writeValueAsString(document);
			DataCache dc = new DataCache(currentIndex, "prop_def", recordJson);
			elasticSearchService.addData(dc);

			kafkaTemplate.send(topic, recordJson);

		} catch (Exception e) {
			logger.error("Write data to elasticsearch error.", e);
		}

	}

	@KafkaListener(topics = "${spring.kafka.topics.data_import}")
	public void receive(String message) {
		System.err.println(message);
	}

	@Override
	public void close() {
		if (logger.isDebugEnabled()) {
			logger.debug("Elasticsearch writer closed.");
		}
	}

}
