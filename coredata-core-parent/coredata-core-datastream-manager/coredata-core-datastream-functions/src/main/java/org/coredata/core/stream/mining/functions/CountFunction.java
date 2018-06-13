package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class CountFunction extends AbsFunction {

	@SuppressWarnings({ "rawtypes" })
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		Object expObj = FunctionUtils.getJavaObject(exp, env);
		if (expObj == null)
			return new CustomerDouble(null);
		if (expObj instanceof List) {
			List results = (List) expObj;
			return new CustomerDouble(results.size());
		}
		return new CustomerDouble(0);
	}

	@Override
	public String getName() {
		return "countByKey";
	}

}
