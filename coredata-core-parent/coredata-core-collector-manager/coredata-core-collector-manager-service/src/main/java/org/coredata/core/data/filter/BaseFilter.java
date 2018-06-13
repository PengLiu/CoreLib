package org.coredata.core.data.filter;

import java.io.IOException;

import org.coredata.core.data.Record;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseFilter implements IFilter {
	
	ObjectMapper mapper = new ObjectMapper();
	//动作类型
	String actionType;
	//动作规则
	String actionRule;

	public abstract void doFilter(int columnIndex, Record record);

	@Override
	public IFilter init(String config) {
		try {
			JsonNode configJson = mapper.readTree(config);
			actionType = configJson.get("actionType").textValue();
			actionRule = configJson.get("actionRule").textValue();
			if(configJson.get("actionRule").isContainerNode()) {
				actionRule = configJson.get("actionRule").toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/*
	 * 提出最后处理的公共方法
	 */
	protected void processResult(Object data,int columnIndex,Record record) {
		if (FilterEnum.ActionType.Replace.name().equals(actionType)) {
			record.set(columnIndex, data);
		} else if (FilterEnum.ActionType.SaveAndAdd.name().equals(actionType)) {
			record.add(data);
		}
	}

}
