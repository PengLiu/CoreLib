package org.coredata.core.framework.agentmanager.util;

import org.springframework.util.StringUtils;

/**
 * 字符串工具类，用于对字符串相关操作
 *
 * @author sushi
 */
public class StringUtil {

    private static final String DEFAULT_SPLITER = ",";

    /**
     * 用于将字符串数组转为String字符串
     *
     * @param arrays
     * @param spliter
     * @return
     */
    public static String join(String[] arrays, String spliter) {
        String result = "";
        if (arrays == null || arrays.length <= 0) {
            return result;
        }
        StringBuilder sb = new StringBuilder();
        for (String arr : arrays) {
            sb.append(StringUtils.isEmpty(spliter) ? DEFAULT_SPLITER : spliter).append(arr);
        }
        result = sb.substring(1).toString();
        return result;
    }

}
