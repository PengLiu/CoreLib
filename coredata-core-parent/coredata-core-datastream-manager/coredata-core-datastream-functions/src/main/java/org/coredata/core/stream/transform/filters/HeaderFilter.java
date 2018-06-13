package org.coredata.core.stream.transform.filters;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.coredata.core.stream.util.TransformUtil;
import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;

public class HeaderFilter extends AbsFilter {

	private static final long serialVersionUID = -1106951431659515646L;

	public HeaderFilter() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(TransformData response, FilterChain chain) {
		try {
			LinkedHashMap<String, List<Object>> returnData = new LinkedHashMap<>();
			JsonNode json = response.getResultJson();
			Iterator<String> fieldNames = json.fieldNames();
			while (fieldNames.hasNext()) {
				String key = fieldNames.next();
				List<Object> data = mapper.readValue(json.get(key).traverse(), List.class);
				returnData.put(key, data);
			}
			String result = TransformUtil.transformResultSet(returnData);
			json = mapper.readTree(result);
			response.setResultJson(json);
		} catch (Throwable e) {
			logError(e, response);
		}
		chain.doFilter(response);
	}

}
