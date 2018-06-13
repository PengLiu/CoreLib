package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.common.Restype;

import com.alibaba.fastjson.JSON;

public class RestypeConverter implements AttributeConverter<Restype, String> {

	@Override
	public String convertToDatabaseColumn(Restype model) {
		return JSON.toJSONString(model);
	}

	@Override
	public Restype convertToEntityAttribute(String json) {
		return JSON.parseObject(json, Restype.class);
	}

}
