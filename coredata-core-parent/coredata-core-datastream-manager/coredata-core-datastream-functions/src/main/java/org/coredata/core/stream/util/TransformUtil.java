package org.coredata.core.stream.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;

public class TransformUtil {

	private static final String REGEX = "(?<=\\$\\{)(.+?)(?=\\})";

	private static final String POINT = ",";

	/**
	 * 该方法用于抽取生成规则中的变量
	 * @return
	 */
	public static List<String> extractionParams(String format) {
		Pattern compile = Pattern.compile(REGEX);
		Matcher matcher = compile.matcher(format);
		List<String> params = new ArrayList<>();
		while (matcher.find()) {
			String param = matcher.group();
			params.add(param);
		}
		return params;
	}

	/**
	 * 该方法用于进行结果集去表头转换
	 * @return
	 */
	public static String transformResultSet(LinkedHashMap<String, List<Object>> resultSet) {
		List<Object> cols = new ArrayList<>();
		Object[] values = null;
		boolean single = resultSet.size() == 1;
		for (Map.Entry<String, List<Object>> entry : resultSet.entrySet()) {
			List<Object> value = entry.getValue();
			//修改cols集合的数据类型为object，有可能为integer类型,也有可能为string类型，有可能是其他类型
			if (cols.size() == 0) //说明是第一次初始化，放入表头
				value.forEach(v -> cols.add(v instanceof String ? ((String) v).trim() : v));
			else {//放入对应的值
				if (values == null) {
					values = new Object[value.size()];
					for (int i = 0; i < values.length; i++)
						values[i] = value.get(i);
				} else {
					for (int i = 0; i < values.length; i++)
						values[i] = values[i] + POINT + value.get(i);
				}
			}
		}
		if (single)
			return JSON.toJSONString(cols);
		//最后再重新放入相关值
		Map<String, Object> r = new HashMap<>();
		for (int i = 0; i < cols.size(); i++) {
			r.put(cols.get(i) instanceof String ? ((String) cols.get(i)).trim() : String.valueOf(cols.get(i)),
					values[i] == null ? "" : values[i].toString().split(POINT));
		}
		return JSON.toJSONString(r);
	}

	public static JsonNode getParentJsonNode(List<String> keys, int keynum, JsonNode content) {
		JsonNode parent = null;
		for (int i = 0; i < keynum - 1; i++) {
			if (parent == null) {
				parent = content.get(keys.get(i));
			} else {
				parent = parent.get(keys.get(i));
			}
		}
		if (keynum >= 2) {
			return parent;
		}
		return content;
	}

	public static JsonNode getCurrentJsonNode(List<String> keys, int keynum, JsonNode content) {
		JsonNode current = null;
		for (int i = 0; i < keynum; i++) {
			if (current == null) {
				current = content.get(keys.get(i));
			} else {
				current = current.get(keys.get(i));
			}
		}
		return current;
	}

}
