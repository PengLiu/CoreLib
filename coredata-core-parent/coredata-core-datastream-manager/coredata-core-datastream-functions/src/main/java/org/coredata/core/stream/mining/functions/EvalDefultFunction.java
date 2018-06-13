package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class EvalDefultFunction extends AbsFunction {

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject defultVal) {
		String defultV = FunctionUtils.getStringValue(defultVal, env);
		Object value = exp.getValue(env);
		if (value == null)
			return new AviatorString(defultV);
		try {
			if (value instanceof Number)
				return new CustomerDouble((Number) value);
			else if (value instanceof String && !"".equals(value.toString()))
				return new AviatorString(value.toString());
		} catch (Throwable e) {
			logError(e);
		}
		return new AviatorString(defultV);
	}

	@Override
	public String getName() {
		return "eval_defult";
	}

}