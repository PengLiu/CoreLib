package org.coredata.core.framework.util;

import com.alibaba.fastjson.JSON;

import java.util.*;

/**
 * 清洗工具类
 * @author sushiping
 *
 */
public class TransformUtil {

	public static final String WITHHEADER_YES = "yes";

	public static final String WITHHEADER_NO = "no";

	private static final String BLANK = " ";

	/**
	 * 该方法用于进行结果集表头转换
	 * @return
	 */
	public static List<Map<String, String>> transformResultSet(LinkedHashMap<String, List<String>> resultSet, String withheader) {
		List<Map<String, String>> results = new ArrayList<>();
		switch (withheader) {
		case WITHHEADER_YES:
			for (Map.Entry<String, List<String>> entry : resultSet.entrySet()) {
				List<String> value = entry.getValue();
				if (results.size() == 0) {
					for (String v : value) {
						Map<String, String> r = new HashMap<>();
						r.put(entry.getKey(), v);
						results.add(r);
					}
				} else {
					for (int i = 0; i < results.size(); i++) {
						Map<String, String> m = results.get(i);
						m.put(entry.getKey(), value.get(i));
					}
				}
			}
			break;
		case WITHHEADER_NO://如果不要表头
			List<String> cols = new ArrayList<>();
			String[] values = null;
			for (Map.Entry<String, List<String>> entry : resultSet.entrySet()) {
				List<String> value = entry.getValue();
				if (cols.size() == 0) //说明是第一次初始化，放入表头
					value.forEach(v -> cols.add(v));
				else {//放入对应的值
					if (values == null) {
						values = new String[value.size()];
						for (int i = 0; i < values.length; i++)
							values[i] = value.get(i);
					} else {
						for (int i = 0; i < values.length; i++)
							values[i] = values[i] + BLANK + value.get(i);
					}
				}
			}
			//最后再重新放入相关值
			for (int i = 0; i < cols.size(); i++) {
				Map<String, String> r = new HashMap<>();
				r.put(cols.get(i), values[i]);
				results.add(r);
			}
			break;
		}
		return results;
	}

	/**
	 * 该方法用于将采集回来的数据进行表头转换，供实例化资源使用
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String, String> transformToInstance(LinkedHashMap<String, String> resultSet, String withheader) {
		LinkedHashMap<String, String> results = new LinkedHashMap<>();
		switch (withheader) {
		case WITHHEADER_YES://如果需要表头，直接返回原值
			results.putAll(resultSet);
			break;
		case WITHHEADER_NO://如果不要表头，转换表头数据
			List<String> cols = new ArrayList<>();
			String[] values = null;
			for (Map.Entry<String, String> entry : resultSet.entrySet()) {
				String value = entry.getValue();
				//将value转换为数组
				List<String> keys = (List<String>) JSON.parse(value);
				if (cols.size() == 0) //说明是第一次初始化，放入表头
					keys.forEach(v -> cols.add(v));
				else {//放入对应的值
					if (values == null) {
						values = new String[keys.size()];
						for (int i = 0; i < values.length; i++)
							values[i] = keys.get(i);
					} else {
						for (int i = 0; i < values.length; i++)
							values[i] = values[i] + BLANK + keys.get(i);
					}
				}
			}
			//最后再重新放入相关值
			for (int i = 0; i < cols.size(); i++)
				results.put(cols.get(i), "[\"" + values[i] + "\"]");
			break;
		}
		return results;
	}

}
