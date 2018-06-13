package org.coredata.core.framework.agentmanager.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 日志工具类，用于输出相关日志信息
 * @author sushi
 *
 */
public class LogUtil {

	public static String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

}
