package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class GetIndexValueFunction extends AbsFunction {

	@Override
	public String getName() {
		return "getIndexValue";
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject index, AviatorObject content, AviatorObject operation, AviatorObject splite) {

		//获取值对象
		Object contentValue = FunctionUtils.getJavaObject(content, env);

		//获取index
		String indexStr = FunctionUtils.getStringValue(index, env);
		//获取分割字符串
		String spliteStr = FunctionUtils.getStringValue(splite, env);
		String[] indexs = indexStr.split(spliteStr);
		//获取操作字符串
		String operationStr = FunctionUtils.getStringValue(operation, env);

		List<String> values = new ArrayList<String>();
		if (contentValue instanceof List) {
			List arrayNode = (List) contentValue;
			for (String i : indexs) {
				if (!StringUtils.isEmpty(i) && arrayNode.size() > Integer.valueOf(i))
					values.add(String.valueOf(arrayNode.get(Integer.valueOf(i))));
			}
		} else {
			values.add(String.valueOf(contentValue));
		}

		Object result = null;

		switch (operationStr) {
		case "SUM":
			double sum = 0.00D;
			for (String v : values) {
				sum = sum + Double.parseDouble(v);
			}
			result = sum;
			break;
		case "LITESET":
			Set<String> liteSet = new HashSet<String>();
			for (String v : values) {
				liteSet.add(v);
			}
			result = StringUtils.join(liteSet, ",");
			break;
		case "LITE":
			List<String> lite = new ArrayList<String>();
			for (String v : values) {
				lite.add(v);
			}
			result = StringUtils.join(lite, ",");
			break;
		case "COUNT":
			//同名计数
			int num = 0;
			if (values.size() > 0) {
				String val = values.get(0);
				if (contentValue instanceof List) {
					List arrayNode = (List) contentValue;
					for (Object node : arrayNode) {
						if (val.equals(String.valueOf(node)))
							num++;
					}
				} else {
					num = values.size();
				}
			}
			result = num;
			break;
		case "ISNOTNULL":
			//待优化
			result = String.valueOf(values.size() != 0 ? "ok" : "error");
			break;
		default:
			result = StringUtils.join(values, ",");
			break;
		}
		if (result instanceof Number) {
			return new CustomerDouble((Number) result);
		}
		return new AviatorString(result == null ? null : result.toString());

	}

}
