package org.coredata.core;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;

public class IndexUtils {

	public static final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy.MM.dd");

	public String daySuffix() {
		return dateFormat.format(new Date(System.currentTimeMillis()));
	}

	public static String getDaySuffix() {
		return dateFormat.format(new Date(System.currentTimeMillis()));
	}

}
