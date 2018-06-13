package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;

public class AvgFunction extends AbsFunction {

	@Override
	public String getName() {
		return "avg";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		Object expObj = exp.getValue(env);
		if (expObj instanceof List) {
			double result = 0;
			List results = (List) expObj;
			for (Object r : results)
				result += Double.valueOf(r.toString());
			return new CustomerDouble(result / results.size());
		} else {
			if (expObj instanceof Number) {
				Number num = (Number) expObj;
				return new CustomerDouble(num.doubleValue());
			}
		}
		return new CustomerDouble(null);
	}

}
