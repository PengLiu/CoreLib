package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class SumArrayFunction extends AbsVariadicFunction {

	@Override
	public String getName() {
		return "sumArray";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... params) {
		AviatorObject operator = params[params.length - 1];//获取计算参数
		String opera = FunctionUtils.getStringValue(operator, env);
		List<Double> results = new ArrayList<>();//保存数组执行后的结果
		List<StringBuilder> execs = new ArrayList<>();
		for (int i = 0; i < params.length - 1; i++) {
			List result = (List) FunctionUtils.getJavaObject(params[i], env);
			if (result == null)
				continue;
			for (int j = 0; j < result.size(); j++) {
				StringBuilder ex = null;
				if (i == 0) {
					ex = new StringBuilder();
					ex.append(result.get(j));
					execs.add(ex);
				} else {
					ex = execs.get(j);
					ex.append(opera).append(result.get(j));
					execs.remove(j);
					execs.add(j, ex);
				}
			}
		}
		for (StringBuilder exec : execs) {
			Double execute = (Double) AviatorEvaluator.execute(exec.toString());
			if (execute == null)
				continue;
			results.add(execute);
		}
		Double result = null;
		for (Double r : results) {
			result = (result == null ? 0 : result.doubleValue()) + r.doubleValue();
		}
		return new CustomerDouble(result);
	}

}
