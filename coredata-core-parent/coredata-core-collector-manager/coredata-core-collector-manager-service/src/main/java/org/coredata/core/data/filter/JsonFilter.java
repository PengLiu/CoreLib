package org.coredata.core.data.filter;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonFilter extends BaseFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(JsonFilter.class);

	@Override
	public void doFilter(int columnIndex, Record record) {
		if(StringUtils.isNoneBlank(actionType) && StringUtils.isNoneBlank(actionRule)) {
			try {
				JsonNode json = null;
				Object oldData = record.get(columnIndex);
				if(oldData instanceof String) {
					json = mapper.readTree((String)oldData);
				}
				if(null != json) {
					JsonNode data = json.get(actionRule);
					if(null != data) {
						String newString = "";
						if(data.isContainerNode()) {
							newString = data.toString();
						}else {
							newString = data.textValue();
						}
						processResult(newString, columnIndex, record);
					}
				}
			} catch (Exception e) {
				logger.info(" transfor error:", e);
			}
		}

	}
}
