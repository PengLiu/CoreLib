package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;

public class RemoveInvalidFunction extends AbsVariadicFunction {

	@Override
	public String getName() {
		return "removeInvalidStr";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
		if (args == null || args.length <= 0)
			return new CustomerDouble(null);
		Object exp = args[0].getValue(env);
		List<Object> results = new ArrayList<>();
		if (exp instanceof List) {
			List exps = (List) exp;
			for (Object obj : exps) {
				String expStr = obj.toString();
				//循环替换
				for (int i = 1; i < args.length; i++) {
					String replace = args[i].getValue(env).toString();
					expStr = expStr.replace(replace, "");
				}
				//替换后直接放入结果集合
				results.add(expStr);
			}
		} else {
			String expStr = exp.toString();
			//循环替换
			for (int i = 1; i < args.length; i++) {
				String replace = args[i].getValue(env).toString();
				expStr = expStr.replace(replace, "");
			}
			//替换后直接放入结果集合
			results.add(expStr);
		}
		return new AviatorRuntimeJavaType(results);
	}

}
