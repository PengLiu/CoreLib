package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.decision.DecisionModel;

import com.alibaba.fastjson.JSON;

public class DecisionConverter implements AttributeConverter<DecisionModel, String> {

	@Override
	public String convertToDatabaseColumn(DecisionModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public DecisionModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, DecisionModel.class);
	}

}
