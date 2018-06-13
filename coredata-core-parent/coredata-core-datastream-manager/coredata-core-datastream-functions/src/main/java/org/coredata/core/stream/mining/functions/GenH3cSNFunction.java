package org.coredata.core.stream.mining.functions;

import java.io.File;
import java.util.Map;

import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;

public class GenH3cSNFunction extends AbsFunction {

	@Override
	public String getName() {
		return "genH3cSN";
	}

	@Override
	public AviatorObject call(Map<String, Object> env, AviatorObject exp) {
		String expStr = exp.getValue(env).toString();
		String stringFromASCII = getStringFromASCII(expStr, "\\.");
		return new AviatorString(stringFromASCII);
	}

	private String getStringFromASCII(String ascii, String prefix) {
		String[] t_split = ascii.split(File.separator + prefix);
		char[] t_chars = new char[t_split.length - 1];
		for (int t_i = 1; t_i < t_split.length; t_i++) {
			t_chars[t_i - 1] = (char) Integer.parseInt(t_split[t_i]);
		}
		return new String(t_chars);
	}
}
