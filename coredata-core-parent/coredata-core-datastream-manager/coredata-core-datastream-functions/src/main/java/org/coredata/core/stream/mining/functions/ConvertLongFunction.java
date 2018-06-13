package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;

/**
 * 将表达式中数字转为自定义Long型对象
 * @author sushi
 *
 */
public class ConvertLongFunction extends AbsFunction {

	@Override
	public String getName() {
		return "convertLong";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject num) {
		Number number = FunctionUtils.getNumberValue(num, env);
		return new CustomerLong(number);
	}

}
