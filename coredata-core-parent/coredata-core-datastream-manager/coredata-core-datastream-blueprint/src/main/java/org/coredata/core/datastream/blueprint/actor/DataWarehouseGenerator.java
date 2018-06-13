package org.coredata.core.datastream.blueprint.actor;

import org.coredata.core.ElasticsearchService;
import org.coredata.core.IndexUtils;
import org.coredata.core.datastream.blueprint.vo.DataWarehouseFragment;
import org.coredata.core.olap.model.entities.OlapDimIndex;
import org.coredata.core.olap.model.entities.OlapFieldDef;
import org.coredata.core.olap.model.entities.OlapModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.Done;
import akka.actor.AbstractActor;

@Component
@Scope("prototype")
public class DataWarehouseGenerator extends AbstractActor {

	private Logger logger = LoggerFactory.getLogger(DataWarehouseGenerator.class);

	private ObjectMapper mapper = new ObjectMapper();

	private OlapModel olapModel;

	private String token;

	@Autowired
	private ElasticsearchService elasticsearchService;

	public DataWarehouseGenerator(DataWarehouseFragment fragment, String token) {
		this.olapModel = fragment.getModel();
		this.token = token;
	}

	@Override
	public Receive createReceive() {

		return receiveBuilder().match(String.class, record -> {

			JsonNode msgJson = mapper.readTree(record);

			JsonNode olap = msgJson.get("data");
			JsonNode srcIndex = olap.get("srcIndex");

			if (srcIndex == null) {
				logger.error("No source index field found.");
				getSender().tell(Done.getInstance(), getSelf());
				return;
			}

			ObjectNode factDoc = mapper.createObjectNode();

			//创建fact对象
			for (OlapFieldDef fieldDef : olapModel.getFactIndex().getFactFields()) {
				String fieldName = fieldDef.getName();
				String fieldType = fieldDef.getFieldType();
				JsonNode fieldData = olap.get(fieldName);
				if (fieldData != null) {
					createDoc(fieldType, fieldName, fieldData.asText(), factDoc);
				}
			}
			for (OlapDimIndex dim : olapModel.getFactIndex().getDimensions()) {
				String dimSrcIndex = dim.getSrcIndex();
				String val = olap.get(dim.getFactRefId()).asText();
				String result = elasticsearchService.queryByCondition(dimSrcIndex, dim.getDimRefId(), val);
				if (StringUtils.isEmpty(result)) {
					continue;
				}
				JsonNode dimensionIndex = mapper.readTree(result);

				ObjectNode dimDoc = mapper.createObjectNode();
				for (OlapFieldDef fieldDef : dim.getDeminsionFields()) {
					createDoc(fieldDef.getFieldType(), fieldDef.getName(), dimensionIndex.get(fieldDef.getName()).asText(), dimDoc);
				}
				factDoc.set(dim.getRefName(), dimDoc);
			}

			String indexName = olapModel.getFactIndex().getIndexName() + "_" + IndexUtils.getDaySuffix();
			elasticsearchService.save(mapper.writeValueAsBytes(factDoc), indexName, "prop_def");
			getSender().tell(Done.getInstance(), getSelf());
		}).build();

	}

	private void createDoc(String fieldType, String fieldName, String val, ObjectNode doc) {
		switch (fieldType.toLowerCase()) {
		case "date":
			doc.put(fieldName, Long.parseLong(val));
			break;
		case "long":
			doc.put(fieldName, Long.parseLong(val));
			break;
		case "integer":
			doc.put(fieldName, Integer.parseInt(val));
			break;
		case "double":
			doc.put(fieldName, Double.parseDouble(val));
			break;
		case "float":
			doc.put(fieldName, Float.parseFloat(val));
			break;
		case "boolean":
			doc.put(fieldName, Boolean.parseBoolean(val));
			break;
		case "text":
		case "keyword":
		case "ip":
			doc.put(fieldName, val);
			break;
		}
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}