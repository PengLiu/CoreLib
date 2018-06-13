package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class ConditionOrFunction extends AbsVariadicFunction {

	@Override
	public String getName() {
		return "conditionOr";
	}

	@Override
	public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... exps) {
		String result = null;
		for (AviatorObject exp : exps) {
			if (exp == null || exp.getValue(env) == null)
				continue;
			if (exp instanceof AviatorNumber) {
				Number number = FunctionUtils.getNumberValue(exp, env);
				result = number == null ? null : String.valueOf(number.doubleValue());
			} else
				result = FunctionUtils.getStringValue(exp, env);
			if (result == null)
				continue;
			return new AviatorString(result);
		}
		return new AviatorString(null);
	}

}
