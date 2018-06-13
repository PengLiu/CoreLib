package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.mining.DataminingModel;

import com.alibaba.fastjson.JSON;

public class MiningConverter implements AttributeConverter<DataminingModel, String> {

	@Override
	public String convertToDatabaseColumn(DataminingModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public DataminingModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, DataminingModel.class);
	}

}
