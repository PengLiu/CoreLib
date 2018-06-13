package org.coredata.core.framework.agentmanager.util;

import java.sql.Timestamp;

/**
 * 日期时间工具类，未来可在此添加相关方法
 * @author sushi
 *
 */
public class DatetimeUtil {

	/**
	 * 该方法用于获取当前系统时间戳
	 * @return
	 */
	public static Timestamp getCurrentTimestamp() {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		return t;
	}

}
