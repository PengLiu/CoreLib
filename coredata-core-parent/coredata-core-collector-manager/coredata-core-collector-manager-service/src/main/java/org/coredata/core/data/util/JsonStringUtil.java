package org.coredata.core.data.util;

import org.apache.commons.lang3.StringUtils;

public class JsonStringUtil {

	public static boolean isEmpty(String json) {
		return StringUtils.isEmpty(json) || json.equals("{}");
	}
	public static boolean isNotEmpty(String json) {
		return !isEmpty(json);
	}
}
