package org.coredata.core.model.converter;

import javax.persistence.AttributeConverter;

import org.coredata.core.model.discovery.DiscoveryModel;

import com.alibaba.fastjson.JSON;

public class DiscoveryConverter implements AttributeConverter<DiscoveryModel, String> {

	@Override
	public String convertToDatabaseColumn(DiscoveryModel model) {
		return JSON.toJSONString(model);
	}

	@Override
	public DiscoveryModel convertToEntityAttribute(String json) {
		return JSON.parseObject(json, DiscoveryModel.class);
	}

}
