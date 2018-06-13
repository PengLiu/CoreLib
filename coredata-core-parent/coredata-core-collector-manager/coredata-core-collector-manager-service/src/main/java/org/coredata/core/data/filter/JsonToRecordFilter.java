package org.coredata.core.data.filter;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonToRecordFilter extends BaseFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(JsonToRecordFilter.class);

	@Override
	public void doFilter(int columnIndex, Record record) {
		if(StringUtils.isNoneBlank(actionType) && StringUtils.isNoneBlank(actionRule)) {
			try {
				String json = String.valueOf(record.get(columnIndex));
				if(StringUtils.isNotBlank(json)) {
					Map<String, Object> dataMap = mapper.readValue(json, Map.class);
					String[] keyStr = actionRule.split(",");
					for(String key:keyStr) {
						if(StringUtils.isBlank(key))
							continue;
						Object data = dataMap.get(key);
						record.add(data);
					}
				}
			}catch (Exception e) {
				logger.info(" transfor error:", e);
			}
		}

	}
}
