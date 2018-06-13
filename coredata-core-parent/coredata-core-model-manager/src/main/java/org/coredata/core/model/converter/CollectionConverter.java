package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.collection.CollectionModel;

import com.alibaba.fastjson.JSON;

public class CollectionConverter implements AttributeConverter<CollectionModel, String> {

	@Override
	public String convertToDatabaseColumn(CollectionModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public CollectionModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, CollectionModel.class);
	}

}
