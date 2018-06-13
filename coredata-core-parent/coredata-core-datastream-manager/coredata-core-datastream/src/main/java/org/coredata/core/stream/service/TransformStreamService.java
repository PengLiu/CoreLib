package org.coredata.core.stream.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.transform.filters.FilterChain;
import org.coredata.core.stream.vo.TransformData;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransformStreamService extends AbstractStreamService {

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public String processTransform(FilterChain chain, Map<String, Object> source) throws Throwable {
		boolean success = Boolean.parseBoolean(source.get("success").toString());
		String finishTime = source.get("finishTime").toString();
		String taskTime = source.get("tasktime").toString();
		String name = source.get("name").toString();
		String instanceId = source.get("instanceId").toString();
		String index = source.get("index") == null ? "1" : source.get("index").toString();
		String modelid = source.get("modelId").toString();
		String nodeId = source.get("nodeId").toString();
		String content = success && source.get("msg") != null ? source.get("msg").toString() : "";
		String params = source.get("params") == null ? null : source.get("params").toString();
		String customerId = source.get("customerId") == null ? null : source.get("customerId").toString();
		String type = source.get("type") == null ? null : source.get("type").toString();

		TransformData response = new TransformData();
		response.setFinishTime(Long.valueOf(finishTime));
		response.setInstanceId(instanceId);
		response.setModelid(modelid);
		response.setName(name);
		response.setTasktime(Long.valueOf(taskTime));
		response.setNodeId(nodeId);
		response.setResultJson(StringUtils.isEmpty(content) ? null : mapper.readTree(content));
		response.setMsg(content);
		response.setParams(params);
		response.setIndex(index);
		response.setCustomerId(customerId);
		response.setType(type);

		if (success) {
			chain.doFilter(response);
			response.setResult(mapper.writeValueAsString(response.getResultJson()));
		} else {
			response.setError(true);
			response.setErrMsg(source.get("err").toString());
		}
		return mapper.writeValueAsString(response);
	}

}
