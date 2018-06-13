package org.coredata.core.stream.mining.functions;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

public class IsNotNullFunction extends AbsFunction {

	@Override
	public String getName() {
		return "isNotNullBoolean";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = FunctionUtils.getStringValue(exp, env);
		if (StringUtils.isEmpty(expStr)) {
			return AviatorBoolean.FALSE;
		}
		return AviatorBoolean.TRUE;
	}

}
