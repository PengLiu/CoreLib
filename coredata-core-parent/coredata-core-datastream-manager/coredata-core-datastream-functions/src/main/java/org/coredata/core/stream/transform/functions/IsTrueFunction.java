package org.coredata.core.stream.transform.functions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class IsTrueFunction extends AbsFunction {

	private String paramExp = "\\$\\{(.*?)\\}";
	private Pattern p = Pattern.compile(paramExp);

	@Override
	public String getName() {
		return "isTrue";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = FunctionUtils.getStringValue(exp, env);
		TransformData data = (TransformData) env.get(COLLET_DATA);
		JsonNode result = data.getResultJson();
		Matcher matcher = p.matcher(expStr);
		while (matcher.find()) {
			String replaceKey = matcher.group();
			String key = matcher.group(1);
			JsonNode node = null;
			if (result instanceof ArrayNode) {
				for (JsonNode jn : result) {
					node = jn.get(key);
					if (node != null)
						break;
				}
			} else {
				node = result.get(key);
			}
			if (node == null) {
				expStr = expStr.replace(replaceKey, "null");
			} else if (node instanceof ArrayNode) {
				ArrayNode arrays = (ArrayNode) node;
				if (arrays.size() > 0) {
					expStr = expStr.replace(replaceKey, arrays.get(0).asText());
				} else {
					expStr = expStr.replace(replaceKey, "null");
				}
			} else {
				expStr = expStr.replace(replaceKey, node.asText());
			}
		}
		boolean expvalue = (boolean) AviatorEvaluator.execute(expStr);

		return new AviatorString(String.valueOf(expvalue));
	}

}
