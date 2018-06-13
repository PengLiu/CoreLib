package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class GetIndexFunction extends AbsFunction {

	@Override
	public String getName() {
		return "getIndex";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject array, AviatorObject content, AviatorObject split) {

		//获取对应表达式
		String arrayStr = FunctionUtils.getStringValue(array, env);
		//获取目标值
		String contentStr = FunctionUtils.getStringValue(content, env);
		//获取分割字符串
		String splitStr = FunctionUtils.getStringValue(split, env);
		String[] arrayInfo = arrayStr.split(splitStr);
		List<String> result=new ArrayList<String>();
		
		for (int i = 0; i < arrayInfo.length; i++) {
			if (arrayInfo[i].equals(contentStr))
				result.add(String.valueOf(i));
		}
		return new AviatorString(StringUtils.join(result, splitStr));
	}
}
