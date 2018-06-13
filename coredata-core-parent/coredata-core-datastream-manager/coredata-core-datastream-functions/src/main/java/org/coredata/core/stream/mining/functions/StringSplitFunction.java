package org.coredata.core.stream.mining.functions;

import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class StringSplitFunction extends AbsFunction {

	@Override
	public String getName() {
		return "stringSplit";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject spliter, AviatorObject index) {
		Object expObj = exp.getValue(env);//获取对应表达式
		String spliterStr = FunctionUtils.getStringValue(spliter, env);//获取对应拆分字符
		Number num = FunctionUtils.getNumberValue(index, env);
		int indexNum = num.intValue();//获取对应拆分后索引位置
		String result = "";
		String expStr = null;
		if (expObj instanceof List) {
			List results = (List) expObj;
			expStr = results.get(0).toString();
		} else {
			expStr = expObj.toString();
		}
		//替换之后，处理相关数据
		String[] results = expStr.split(spliterStr);//根据拆分符拆分对应数据
		if (indexNum < results.length)
			result = results[indexNum];
		return new AviatorString(result);
	}

}
