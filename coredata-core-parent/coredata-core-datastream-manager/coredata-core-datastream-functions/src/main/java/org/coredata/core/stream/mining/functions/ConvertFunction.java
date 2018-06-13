package org.coredata.core.stream.mining.functions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.util.common.UnitUtils;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

/**
 * 单位转换
 * @author cheng
 *
 */
public class ConvertFunction extends AbsFunction {

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject type) {

		Object value = exp.getValue(env);
		if (value == null)
			return new AviatorString(null);
		String expStr = value.toString();
		String typeStr = FunctionUtils.getStringValue(type, env);
		try {
			Pattern numPattern = Pattern.compile("^([0-9]*\\.?[0-9]*)");
			Pattern strPattern = Pattern.compile("[a-zA-Z]*$");
			Matcher numMatcher = numPattern.matcher(expStr);
			Matcher strMatcher = strPattern.matcher(expStr);
			numMatcher.find();
			strMatcher.find();
			double val = Double.parseDouble(numMatcher.group());
			String typeVal = strMatcher.group();
			double converedValue = UnitUtils.convertToFix(val, typeVal.toUpperCase(), typeStr);
			return new AviatorDouble(converedValue);
		} catch (Throwable e) {
			logError(e);
		}
		return new AviatorString(null);
	}

	@Override
	public String getName() {
		return "convert";
	}
}
