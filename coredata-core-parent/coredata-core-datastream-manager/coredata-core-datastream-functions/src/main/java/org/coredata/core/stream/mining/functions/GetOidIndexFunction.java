package org.coredata.core.stream.mining.functions;

import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class GetOidIndexFunction extends AbsFunction {

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = exp.getValue(env).toString();
		String oidFromASCII = getOidIndexFromASCII(expStr);
		return new AviatorString(oidFromASCII);
	}

	private static String getOidIndexFromASCII(final String profileName) {
		char[] name = profileName.toCharArray();
		StringBuilder oidindex = new StringBuilder();
		oidindex.append(name.length);
		for (int i = 0; i < name.length; i++) {
			oidindex.append(".").append((int) name[i]);
		}
		return oidindex.toString();
	}

	@Override
	public String getName() {
		return "getOidIndex";
	}

}
