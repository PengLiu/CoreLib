package org.coredata.core.stream.mining.functions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorLong;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class DateFormatTransformFunction extends AbsFunction {

	private static final String NULL_VALUE = "Null";
	private static final String DATE_TYPE = "long";

	@Override
	public String getName() {
		return "dateFormatTransform";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject oldFormat, AviatorObject newFormat) {
		Object expObj = exp.getValue(env);
		String oldDateFormat = FunctionUtils.getStringValue(oldFormat, env);
		String newDateFormat = FunctionUtils.getStringValue(newFormat, env);
		try {
			if (expObj == null || NULL_VALUE.equals(expObj.toString()))
				return new AviatorString(null);
			String expStr = expObj.toString();
			if (expStr.endsWith("Z"))
				expStr = expStr.replace("Z", " UTC");//注意是空格+UTC,此处表示时间格式是UTC格式
			DateFormat oldDate = new SimpleDateFormat(oldDateFormat);
			Date parse = oldDate.parse(expStr);
			if (DATE_TYPE.equals(newDateFormat))
				return new AviatorLong(parse.getTime());
			DateFormat newDate = new SimpleDateFormat(newDateFormat);
			String result = newDate.format(parse);
			return new AviatorString(result);
		} catch (Throwable e) {
			logError(e);
		}
		return new AviatorString(null);
	}
}
