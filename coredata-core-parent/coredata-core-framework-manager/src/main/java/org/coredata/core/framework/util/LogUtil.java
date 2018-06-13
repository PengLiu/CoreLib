package org.coredata.core.framework.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {

	public static String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

}
