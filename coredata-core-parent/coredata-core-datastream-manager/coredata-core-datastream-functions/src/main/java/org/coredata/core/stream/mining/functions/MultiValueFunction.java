package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

/**
 * 该方法用于返回清洗结果中的多值指标
 * @author sue
 *
 */
public class MultiValueFunction extends AbsFunction {

	@Override
	public String getName() {
		return "multiValue";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		try {
			String expStr = FunctionUtils.getStringValue(exp, env);//获取对应表达式
			return new AviatorString(expStr);
		} catch (Throwable e) {
			logError(e);
		}
		return new AviatorString(null);
	}

}
