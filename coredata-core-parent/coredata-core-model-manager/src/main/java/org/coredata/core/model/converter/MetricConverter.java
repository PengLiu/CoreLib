package org.coredata.core.model.converter;

import com.alibaba.fastjson.JSON;
import org.coredata.core.model.common.Metric;

import javax.persistence.AttributeConverter;

public class MetricConverter implements AttributeConverter<Metric, String> {

	@Override
	public String convertToDatabaseColumn(Metric model) {
		return JSON.toJSONString(model);
	}

	@Override
	public Metric convertToEntityAttribute(String json) {
		return JSON.parseObject(json, Metric.class);
	}

}
