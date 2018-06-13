package org.coredata.core.stream.transform.functions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.vo.TransformData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class IsContainFunction extends AbsFunction {

	private String paramExp = "\\$\\{(.*?)\\}";
	private Pattern p = Pattern.compile(paramExp);

	@Override
	public String getName() {
		return "isContain";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject ex) {
		String expStr = FunctionUtils.getStringValue(exp, env);
		String exps = FunctionUtils.getStringValue(ex, env);
		TransformData data = (TransformData) env.get(COLLET_DATA);
		JsonNode result = data.getResultJson();//获取对应采集回来的值
		Matcher matcher = p.matcher(expStr);//将传来的参数进行匹配
		if (matcher.find()) {
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
			if (node == null)
				return new AviatorString("false");
			if (node instanceof ArrayNode) {
				ArrayNode arrays = (ArrayNode) node;
				if (arrays.size() == 0 || (arrays.size() == 1 && ("null".equals(arrays.get(0).asText().toLowerCase()) || arrays.get(0) == null)))
					return new AviatorString("false");
			} else {//如果是字符串，按照字符串处理
				String r = node.asText();
				if (r == null || r.toLowerCase().equals("null")) {
					return new AviatorString("false");
				} else if (!r.contains(exps)) {
					return new AviatorString("false");
				}
			}

			return new AviatorString("true");
		}
		return null;
	}

}
