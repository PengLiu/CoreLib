package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class StrBuilderFunction extends AbsFunction {

	@Override
	public String getName() {
		return "strBuilder";
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp, AviatorObject spliter, AviatorObject isRepeat) {
		String spliterStr = FunctionUtils.getStringValue(spliter, env);//获取对应拆分字符
		String isrepeat = FunctionUtils.getStringValue(isRepeat, env);//是否去重
		Object expObj = FunctionUtils.getJavaObject(exp, env);
		List<String> appends = new ArrayList<>();
		if (expObj instanceof List) {
			List results = (List) expObj;
			for (Object r : results) {
				if (r == null)
					continue;
				if (("true").equals(isrepeat) && !appends.contains(r.toString()))
					appends.add(r.toString());
				else if (("false").equals(isrepeat))
					appends.add(r.toString());

			}
		} else
			appends.add(expObj.toString());
		if (appends.size() <= 0)
			return new AviatorString(null);
		return new AviatorString(String.join(spliterStr, appends));
	}

}
