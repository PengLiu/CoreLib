package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorNumber;
import com.googlecode.aviator.runtime.type.AviatorObject;

/**
 * 保正函数执行结果为正数
 */
public class KeepPostiveFunction extends AbsFunction {

	@Override
	public String getName() {
		return "keepPostive";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject calNum, AviatorObject defaultValue) {
		double defaultNum = FunctionUtils.getNumberValue(defaultValue, env).doubleValue();
		Number calResult = FunctionUtils.getNumberValue(calNum, env);
		if (calResult == null)
			return new CustomerDouble(null);
		try {
			AviatorNumber result = AviatorDouble.valueOf(calResult);
			if (result.doubleValue() >= 0) {
				return new CustomerDouble(result.doubleValue());
			}
			return new CustomerDouble(defaultNum);
		} catch (Throwable e) {
			logError(e);
		}
		return new CustomerDouble(null);
	}

}
