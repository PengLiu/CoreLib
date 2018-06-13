package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

public class IndexFilterFunction extends AbsVariadicFunction {

	@Override
	public String getName() {
		return "indexFilter";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... params) {
		List<Integer> indexs = new ArrayList<>();
		if (params.length <= 0)
			return new AviatorRuntimeJavaType(indexs);
		AviatorObject condition = params[params.length - 1];
		String cdt = condition.stringValue(env);//获取对应条件
		int flag = 0;
		Map<String, Object> mt = new HashMap<>();
		for (int i = 0; i < params.length - 1; i++) {
			String key = ((AviatorJavaType) params[i]).getName();
			Object result = FunctionUtils.getJavaObject(params[i], env);
			if (result instanceof List) {
				List r = (List) result;
				flag = r.size();
				mt.put(key, r);
			} else
				mt.put(key, result);
		}
		//之后根据条件中变量，替换相关值
		Map<String, String> cdtParams = extractionConditionIds(cdt, strPattern, 1);
		Set<Entry<String, String>> entrySet = cdtParams.entrySet();
		for (int i = 0; i < flag; i++) {
			String replaceCdt = cdt;
			for (Entry<String, String> entry : entrySet) {
				Object obj = mt.get(entry.getValue());
				if (obj == null)
					continue;
				if (obj instanceof List) {
					List allIndex = (List) obj;
					Object value = allIndex.get(i);
					replaceCdt = replaceCdt.replace(entry.getKey(), value == null ? "" : covert(value.toString()));
				} else {
					replaceCdt = replaceCdt.replace(entry.getKey(), obj == null ? "" : covert(obj.toString()));
				}
			}
			//替换之后执行，看是否正确
			try {
				Boolean execute = (Boolean) AviatorEvaluator.execute(replaceCdt);
				if (execute == null || !execute)
					continue;
				indexs.add(i);
			} catch (Exception e) {
				logError(e);
			}
		}
		return new AviatorRuntimeJavaType(indexs);
	}

	private String covert(String old) {
		String newWorld = old;
		if (old.contains("'"))
			newWorld = old.replace("'", "\\'");
		return newWorld;
	}

}
