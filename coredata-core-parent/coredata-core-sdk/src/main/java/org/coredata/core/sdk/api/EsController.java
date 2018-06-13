package org.coredata.core.sdk.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.util.EntityUtils;
import org.coredata.core.ElasticsearchService;
import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.service.ImportJobService;
import org.coredata.core.data.vo.ColumnMeta;
import org.coredata.core.data.vo.TableMeta;
import org.coredata.core.data.writers.elasticsearch.FieldDef;
import org.coredata.core.sdk.api.common.ResponseMap;
import org.coredata.core.vo.ElasticSearchCondition;
import org.coredata.core.vo.ElasticSearchNextCondition;
import org.elasticsearch.client.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/v1/es")
public class EsController {

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private ImportJobService service;

	@RequestMapping(value = "/sql/query", method = RequestMethod.POST)
	public ResponseMap query(@RequestBody ElasticSearchCondition condition) throws Exception {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			String query = condition.getQuery();
			if (StringUtils.isEmpty(query)) {
				result = ResponseMap.BadRequestInstance();
				result.setMessage("query is null");
				return result;
			}
			String handleQuery = handleQuery(query);
			condition.setQuery(handleQuery);
			String jsonData = JSON.toJSONString(condition);
			Response resp = elasticsearchService.rawQuery("POST", "/_xpack/sql?format=json", jsonData);
			if (resp != null) {
				byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
				String respStr = new String(bytes);
				if (respStr != null && !respStr.isEmpty()) {
					ObjectMapper mapper = new ObjectMapper();
					ObjectNode er = (ObjectNode) mapper.readTree(respStr);
					result.setResult(er);
				}
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("es sql query：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(value = "/sql/querynext", method = RequestMethod.POST)
	public ResponseMap query(@RequestBody ElasticSearchNextCondition condition) throws Exception {
		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			String jsonData = JSON.toJSONString(condition);
			Response resp = elasticsearchService.rawQuery("POST", "/_xpack/sql?format=json", jsonData);
			if (resp != null) {
				byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
				String respStr = new String(bytes);
				if (respStr != null && !respStr.isEmpty()) {
					ObjectMapper mapper = new ObjectMapper();
					ObjectNode er = (ObjectNode) mapper.readTree(respStr);
					result.setResult(er);
				}
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("es sql query：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@RequestMapping(path = "/job/mapping/{jobId}", method = RequestMethod.GET)
	public ResponseMap getTemplate(@PathVariable String jobId) throws Exception {

		ResponseMap result = ResponseMap.SuccessInstance();
		try {
			DataImportJob job = service.findById(jobId);
			if (job != null && !StringUtils.isEmpty(job.getIndexName())){
				String index = job.getIndexName();
				Response resp = elasticsearchService.rawQuery("GET", "/_template/" + index + "_*", "{}");
				if (resp != null) {
					byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
					String respStr = new String(bytes);

					if (respStr != null && !respStr.isEmpty()) {
						ObjectMapper mapper = new ObjectMapper();
						ObjectNode template = (ObjectNode) mapper.readTree(respStr);
						Iterator<Entry<String, JsonNode>> ite = template.get(index + "_template").get("mappings")
								.get("prop_def").get("properties").fields();
						String tableDef = job.getTableMeta();
						TableMeta tableMeta = JSON.parseObject(tableDef, TableMeta.class);
						List<ColumnMeta> cols = tableMeta.getColumns();
						Map<String, String> comments = new HashMap<>();
						for (ColumnMeta col : cols) {
							comments.put(col.getName(), col.getComment());
						}
						List<FieldDef> fields = new ArrayList<>();
						while (ite.hasNext()) {
							Entry<String, JsonNode> entry = ite.next();
							String key = entry.getKey();
							String type = entry.getValue().get("type").asText();
							FieldDef def = new FieldDef(key, type, comments.get(key));
							fields.add(def);
						}
						result.setResult(fields);
					}
				}
			}else {
				result = ResponseMap.BadRequestInstance();
				result.setMessage("job or indexname is null");
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("es template ：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 在sql中的索引名称后面加*
	 * @param query
	 * @return
	 */
	private String handleQuery(String query) {
		while (query.indexOf("  ") != -1) {
			query = query.replaceAll("  ", " ");
		}
		String[] split = query.split(" ");
		int k = 0;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			if (split[i].equalsIgnoreCase("from")) {
				k = 1;
			}
			sb.append(split[i]).append(" ");
			if (k == 1) {
				i++;
				sb.append(split[i]).append("* ");
				k = 0;
			}
		}
		return sb.toString();
	}

	@RequestMapping(path = "/removeindex/{index}", method = RequestMethod.POST)
	public ResponseMap removeindex(@PathVariable String index) throws Exception {
		ResponseMap result = ResponseMap.SuccessInstance();
		if (StringUtils.isEmpty(index)) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("es removeindex index can not be null");
			return result;
		}
		try {
			Response resp = elasticsearchService.rawQuery("DELETE", "/" + index + "_*", "{}");
			if (resp != null) {
				byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
				String respStr = new String(bytes);
				if (respStr != null && !respStr.isEmpty()) {
					result.setResult(respStr);
				}
			}
		} catch (Throwable e) {
			result = ResponseMap.BadRequestInstance();
			result.setMessage("es removeindex：" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

}
