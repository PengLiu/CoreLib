package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class MaxFunction extends AbsVariadicFunction {

	@Override
	public String getName() {
		return "max";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
		try {
			List<Double> results = new ArrayList<>();
			for (AviatorObject exp : args) {
				if (exp.getValue(env) == null)
					continue;
				Number num = null;
				if (exp instanceof AviatorNumber) {
					num = FunctionUtils.getNumberValue(exp, env);
					results.add(num.doubleValue());
				} else {
					Object value = exp.getValue(env);
					if (value instanceof List) {
						List list = (List) value;
						for (Object l : list)
							results.add(Double.valueOf(l.toString()));
					} else {
						String resultNum = FunctionUtils.getStringValue(exp, env);
						num = Double.valueOf(resultNum);
						results.add(num.doubleValue());
					}
				}
			}
			//根据list排序
			Collections.sort(results, Collections.reverseOrder());
			return new CustomerDouble(results.get(0));//返回最大值
		} catch (Throwable e) {
			logError(e);
		}
		return new CustomerDouble(null);
	}

}
