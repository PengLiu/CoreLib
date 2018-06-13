package org.coredata.core.model.converter;

import com.alibaba.fastjson.JSON;
import org.coredata.core.model.common.VendorType;

import javax.persistence.AttributeConverter;

public class VendorTypeConverter implements AttributeConverter<VendorType, String> {

	@Override
	public String convertToDatabaseColumn(VendorType model) {
		return JSON.toJSONString(model);
	}

	@Override
	public VendorType convertToEntityAttribute(String json) {
		return JSON.parseObject(json, VendorType.class);
	}

}
