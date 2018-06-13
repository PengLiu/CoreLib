package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.common.DevtypeModel;

import com.alibaba.fastjson.JSON;

public class DevtypeConverter implements AttributeConverter<DevtypeModel, String> {

	@Override
	public String convertToDatabaseColumn(DevtypeModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public DevtypeModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, DevtypeModel.class);
	}

}
