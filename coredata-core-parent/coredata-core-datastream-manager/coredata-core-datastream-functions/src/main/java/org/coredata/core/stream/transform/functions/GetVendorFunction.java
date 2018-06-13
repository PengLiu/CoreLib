package org.coredata.core.stream.transform.functions;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.stream.util.LookupTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class GetVendorFunction extends AbsFunction {

	private LookupTool look;

	ObjectMapper mapper = new ObjectMapper();

	public GetVendorFunction(LookupTool look) {
		this.look = look;
	}

	@Override
	public String getName() {
		return "getVendor";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = FunctionUtils.getStringValue(exp, env);
		ArrayNode returnResult = mapper.createArrayNode();
		if (StringUtils.isEmpty(expStr) || "null".equals(expStr)) {
			try {
				return new AviatorString(mapper.writeValueAsString(returnResult));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		JsonNode result = null;
		try {
			result = mapper.readTree(expStr);
			if (result == null)
				return new AviatorString(mapper.writeValueAsString(returnResult));
		} catch (IOException e) {
			try {
				return new AviatorString(mapper.writeValueAsString(returnResult));
			} catch (JsonProcessingException e1) {

			}
		}
		if (result instanceof ArrayNode) {
			ArrayNode arrays = (ArrayNode) result;
			for (JsonNode jsonNode : arrays) {
				String macAddress = jsonNode.asText();
				String vendor = look.getVendor(macAddress);
				if (StringUtils.isEmpty(vendor)) {
					returnResult.add("");
					continue;
				}
				String vendorStr = look.i18n(vendor);
				returnResult.add(vendorStr);
			}
		}
		try {
			return new AviatorString(mapper.writeValueAsString(returnResult));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new AviatorString(null);
	}

}
