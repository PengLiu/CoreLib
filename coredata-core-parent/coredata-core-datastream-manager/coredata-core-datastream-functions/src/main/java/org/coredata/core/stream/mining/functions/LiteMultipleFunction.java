package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class LiteMultipleFunction extends AbsFunction {

	@Override
	public String getName() {
		return "liteMultiple";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject express, AviatorObject concatStr) {

		//获取表达式字符串 例如 {a}{b}{c}
		String expStr = FunctionUtils.getStringValue(express, env);
		//获取拼接字符串
		String concat = FunctionUtils.getStringValue(concatStr, env);		
		//获取对应表达式中数据源的key
		Map<String, String> cdtIds = extractionConditionIds(expStr, AbsFunction.envPattern, 1);
		List<String> result = new ArrayList<>();
		boolean isFirst = true;
		if (cdtIds.size() == 0) {
			result.add(expStr);
		} else {
			expStr = getExpress(expStr,env,cdtIds);
			for (Entry<String, String> entry : cdtIds.entrySet()) {
				//获取变量key值
				String id = entry.getKey();
				String key = entry.getValue();
				Object value = env.get(key);
				if (value instanceof List) {
					List rs = (List) value;
					List<String> tmp = new ArrayList<>();
					for (int i = 0; i < rs.size(); i++) {
						if (isFirst) {
							String nv = expStr.replace(id, rs.get(i).toString());
							tmp.add(nv);
						} else {
							String nv = result.get(i).replace(id, rs.get(i).toString());
							tmp.add(nv);
						}
					}
					result = tmp;
					isFirst = false;
				}
			}
		}
		//替换属性之后，重新拼接结果
		return new AviatorString(StringUtils.join(result, concat));

	}
	
	//替换表达式中非数组的变量
	private String getExpress(String exp, Map<String, Object> env , Map<String, String> cdtIds) {
		String express = exp;
		for (Entry<String, String> entry : cdtIds.entrySet()) {
			String id = entry.getKey();
			String key = entry.getValue();
			
			if (!StringUtils.isEmpty(key)) {
				Object value = env.get(key);
				if (value != null && !(value instanceof List)) {
					String replace = String.valueOf(value);					
					express = exp.replace(id, replace);
				}
			}
		}
		return express;
	}

}
