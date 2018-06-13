package org.coredata.core.data.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransforFilter extends BaseFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(TransforFilter.class);

	@Override
	public void doFilter(int columnIndex, Record record) {
		if(StringUtils.isNoneBlank(actionType) && StringUtils.isNoneBlank(actionRule)) {
			try {
				String oldData = String.valueOf(record.get(columnIndex));
				Map<String, String> ruleMap = mapper.readValue(actionRule, Map.class);
				oldData = process(oldData, ruleMap);
				
				processResult(oldData, columnIndex, record);
			} catch (Exception e) {
				logger.info(" transfor error:", e);
			}
		}
	}
	
	
	private  String process(String oldString,Map<String, String> ruleMap) {
		String defaultRule = ruleMap.get("");
		//有默认值处理
		if(StringUtils.isNotBlank(defaultRule)) {
			List<String> dataList=new ArrayList<>();
			dataList.add(oldString);
			for(String key:ruleMap.keySet()) {
				if("".equals(key)) 
					continue;
				for(int i=0;i<dataList.size();i++) {
					String data = dataList.get(i);
					//不包含 就切
					if(!ruleMap.containsValue(data)) {
						String[] newStr = (" "+data+" ").split(key);
						dataList.remove(i);
						List<String> tempList = new ArrayList<>();
						for(int j=0;j<newStr.length;j++) {
							if(0 != j)
								tempList.add(ruleMap.get(key));
							if(StringUtils.isNotBlank(newStr[j])) {
								tempList.add(newStr[j]);
							}
						}
						dataList.addAll(i, tempList);	
						i = i+tempList.size()-1;
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			for(String temp:dataList) {
				if(ruleMap.containsValue(temp)) {
					sb.append(temp);
				}else {
					sb.append(defaultRule);
				}
			}
			return sb.toString();
		}else {
		//无默认值
			for(String key:ruleMap.keySet()) {
				oldString = oldString.replace(key, ruleMap.get(key));
			}
		}
		
		return oldString;
		
	}
}
