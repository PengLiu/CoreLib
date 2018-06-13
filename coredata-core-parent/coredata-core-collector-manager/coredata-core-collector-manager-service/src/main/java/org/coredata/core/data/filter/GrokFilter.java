package org.coredata.core.data.filter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.coredata.core.data.util.GrokUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class GrokFilter extends BaseFilter {
	
	private final String newPattern = "newPattern";

	@Override
	public void doFilter(int columnIndex, Record record) {
		if(StringUtils.isNoneBlank(actionType) && StringUtils.isNoneBlank(actionRule)) {
			try {
				String oldData = String.valueOf(record.get(columnIndex));
				if(StringUtils.isNotBlank(oldData)) {
					JsonNode actionRuleJson = mapper.readTree(actionRule);
					String matchPattern = actionRuleJson.get("matchPattern").textValue();
					if(StringUtils.isNotBlank(matchPattern)) {
						String newPatternStr = "";
						if(actionRuleJson.has(newPattern)){
							newPatternStr = actionRuleJson.get(newPattern).toString();
						}
						String data = null;
						if(StringUtils.isNotBlank(newPatternStr)) {
							Map<String, String> newPatternMap = mapper.readValue(newPatternStr, Map.class);
							data = GrokUtils.matchToJson(matchPattern, oldData,newPatternMap);
						}else {
							data = GrokUtils.matchToJson(matchPattern, oldData);
						}
						if(StringUtils.isNotBlank(data)) {
							processResult(data, columnIndex, record);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
