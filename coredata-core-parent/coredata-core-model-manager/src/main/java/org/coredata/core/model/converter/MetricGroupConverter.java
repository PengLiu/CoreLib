package org.coredata.core.model.converter;

import com.alibaba.fastjson.JSON;
import org.coredata.core.model.common.MetricGroup;

import javax.persistence.AttributeConverter;

public class MetricGroupConverter implements AttributeConverter<MetricGroup, String> {

	@Override
	public String convertToDatabaseColumn(MetricGroup model) {
		return JSON.toJSONString(model);
	}

	@Override
	public MetricGroup convertToEntityAttribute(String json) {
		return JSON.parseObject(json, MetricGroup.class);
	}

}
