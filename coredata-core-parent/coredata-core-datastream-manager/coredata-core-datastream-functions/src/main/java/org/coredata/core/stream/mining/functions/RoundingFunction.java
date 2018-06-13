package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class RoundingFunction extends AbsFunction {

	@Override
	public String getName() {
		return "rounding";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		Number value = FunctionUtils.getNumberValue(exp, env);
		return new AviatorLong((int) Math.floor(value.doubleValue()));
	}

}
