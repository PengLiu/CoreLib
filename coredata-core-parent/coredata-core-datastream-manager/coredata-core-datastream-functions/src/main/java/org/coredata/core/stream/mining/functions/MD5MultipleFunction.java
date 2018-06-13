package org.coredata.core.stream.mining.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.util.common.MethodUtil;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

/**
 * 数组字符串MD5加密
 *
 * @author cheng
 *
 *
 */
public class MD5MultipleFunction extends AbsFunction {

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject array, AviatorObject split) {
		try {

			String arrayStr = FunctionUtils.getStringValue(array, env);
			String splitStr = FunctionUtils.getStringValue(split, env);
			String[] toMd5s = arrayStr.split(splitStr);
			List<String> md5s = new ArrayList<String>();
			for (String toMd5 : toMd5s) {
				md5s.add(MethodUtil.md5(toMd5));
			}
			return new AviatorString(StringUtils.join(md5s, splitStr));
		} catch (Exception e) {
			e.printStackTrace();
			return new AviatorString(null);
		}
	}

	@Override
	public String getName() {

		return "md5Multiple";
	}

}
