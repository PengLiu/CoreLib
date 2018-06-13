package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class MatchConditionFunction extends AbsFunction {

	@Override
	public String getName() {
		return "matchCondition";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject indexs, AviatorObject exc) {
		List<Integer> needIndexs = (List<Integer>) indexs.getValue(env);//获取满足期望值的条件
		if (CollectionUtils.isEmpty(needIndexs))
			return new AviatorString(null);
		String except = FunctionUtils.getStringValue(exc, env);//获取对应条件的期望值
		Map<String, String> params = extractionConditionIds(except, matchStrPattern, 1);
		if (params.size() <= 0) {
			Object execute = AviatorEvaluator.execute(except);
			if (execute == null)
				return new AviatorString(null);
			if (execute instanceof Number) {
				Number num = (Number) execute;
				return new CustomerDouble(num);
			} else
				return new AviatorString(execute.toString());
		}
		Set<Entry<String, String>> pas = params.entrySet();
		for (Entry<String, String> pa : pas) {
			String key = pa.getKey();
			String value = pa.getValue();
			except = except.replace(key, value);
			Object oldValues = env.get(value);
			if (oldValues == null)
				continue;
			List olds = (List) oldValues;
			List news = new ArrayList();
			for (Integer index : needIndexs) {
				if (index == null)
					continue;
				Object nv = olds.get(index);
				news.add(nv);
			}
			env.put(value, news);
		}
		Object execute = AviatorEvaluator.execute(except, env);
		if (execute instanceof Number) {
			Number num = (Number) execute;
			return new CustomerDouble(num);
		} else
			return new AviatorString(execute.toString());
	}

}
