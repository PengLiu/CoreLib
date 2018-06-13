package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.action.model.ActionModel;

import com.alibaba.fastjson.JSON;

public class ActionConverter implements AttributeConverter<ActionModel, String> {

	@Override
	public String convertToDatabaseColumn(ActionModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public ActionModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, ActionModel.class);
	}

}
