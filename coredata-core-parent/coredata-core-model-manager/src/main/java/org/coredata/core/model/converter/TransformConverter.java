package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.transform.TransformModel;

import com.alibaba.fastjson.JSON;

public class TransformConverter implements AttributeConverter<TransformModel, String> {

	@Override
	public String convertToDatabaseColumn(TransformModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public TransformModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, TransformModel.class);
	}

}
