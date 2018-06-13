package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class SumFunction extends AbsFunction {

	@Override
	public String getName() {
		return "sum";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		Object expObj = exp.getValue(env);
		try {
			String pName = ((AviatorJavaType) exp).getName();
			env.put("paramName", pName);
		} catch (Exception e) {

		}
		if (expObj instanceof List) {
			double result = 0;
			List results = (List) expObj;
			for (Object r : results)
				result += Double.valueOf(r.toString());
			return new CustomerDouble(result);
		} else {
			if (expObj instanceof Number) {
				Number num = (Number) expObj;
				return new CustomerDouble(num.doubleValue());
			}
		}
		return new CustomerDouble(null);
	}

}
