package org.coredata.core.stream.mining.functions;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

/**
 * 正则匹配运算
 * @author cheng
 *
 */
public class MatchRegularFunction extends AbsFunction {

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject regex, AviatorObject groupNum, AviatorObject text) {
		String regexStr = FunctionUtils.getStringValue(regex, env);
		Number num = FunctionUtils.getNumberValue(groupNum, env);
		String textStr = FunctionUtils.getStringValue(text, env);
		try {
			Pattern pattern = Pattern.compile(regexStr);
			Matcher strMatcher = pattern.matcher(textStr);
			String val = "";
			if (strMatcher.find())
				val = strMatcher.group(num.intValue());
			return new AviatorString(val);
		} catch (Throwable e) {
			logError(e);
		}
		return new AviatorString(null);
	}

	@Override
	public String getName() {
		return "matchRegular";
	}
}
