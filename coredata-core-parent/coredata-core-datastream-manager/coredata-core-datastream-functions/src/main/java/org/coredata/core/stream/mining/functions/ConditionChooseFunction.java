package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class ConditionChooseFunction extends AbsFunction {

	@Override
	public String getName() {
		return "conditionChoose";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject condition, AviatorObject exc, AviatorObject def) {
		String excStr = FunctionUtils.getStringValue(exc, env);//获取对应期望值
		String defStr = FunctionUtils.getStringValue(def, env);//获取对应默认值
		try {
			Boolean bol = (Boolean) condition.getValue(env);
			if (bol == null || !bol)
				return new AviatorString(defStr);
			return new AviatorString(excStr);
		} catch (Exception e) {
			logError(e);
		}
		return new AviatorString(defStr);
	}

}
