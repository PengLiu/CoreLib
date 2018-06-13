package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class LiteralsFunction extends AbsVariadicFunction {

	@Override
	public String getName() {
		return "lite";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
		AviatorObject object = args[0];
		Object expObj = object.getValue(env);
		//Object expObj = FunctionUtils.getJavaObject(object, env);
		if (expObj == null)
			return new CustomerDouble(null);
		int index = 0;
		if (args.length > 1) {
			Number num = FunctionUtils.getNumberValue(args[1], env);
			index = num.intValue();
		}
		if (expObj instanceof List) {
			List results = (List) expObj;
			Object r = results.get(index);
			if (r instanceof Integer) {
				Integer d = (Integer) r;
				return new CustomerLong(d.longValue());
			} else if (r instanceof Long) {
				Long d = (Long) r;
				return new CustomerLong(d.longValue());
			} else if (r instanceof Double) {
				Double d = (Double) r;
				return new CustomerDouble(d.doubleValue());
			} else
				return new AviatorString(r.toString());
		} else if (expObj instanceof Integer) {
			Integer d = (Integer) expObj;
			return new CustomerLong(d.longValue());
		} else if (expObj instanceof Long) {
			Long d = (Long) expObj;
			return new CustomerLong(d.longValue());
		} else if (expObj instanceof Double) {
			Double d = (Double) expObj;
			return new CustomerDouble(d.doubleValue());
		}
		return new AviatorString(expObj.toString());
	}
}
