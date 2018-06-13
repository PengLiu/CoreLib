package org.coredata.core.model.converter;

import com.alibaba.fastjson.JSON;
import org.coredata.core.model.common.Vendor;

import javax.persistence.AttributeConverter;

public class VendorConverter implements AttributeConverter<Vendor, String> {

	@Override
	public String convertToDatabaseColumn(Vendor model) {
		return JSON.toJSONString(model);
	}

	@Override
	public Vendor convertToEntityAttribute(String json) {
		return JSON.parseObject(json, Vendor.class);
	}

}
