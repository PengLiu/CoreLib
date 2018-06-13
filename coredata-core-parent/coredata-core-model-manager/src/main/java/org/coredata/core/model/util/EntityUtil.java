package org.coredata.core.model.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 实例化资源所用的工具类
 *
 * @author sushiping
 */
public class EntityUtil {

    private static final Logger logger = Logger.getLogger(EntityUtil.class.getName());

    public static final String LEVEL = "level";

    public static final String NODE_LEVEL = "nodeLevel";

    public static final String CUSTOMER_ID = "customerId";

    public static final String ROOT_TYPE = "root";

    public static final String INSTANCE_ID = "instId";

    public static final String INSTANCE_NAME = "name";

    public static final String INSTANCE_DISPLAY_NAME = "displayName";

    public static final String INSTANCE_INDEX = "index";

    public static final String INSTANCE_RESTYPE = "resType";

    public static final String INSTANCE_FULL_RESTYPE = "resfullType";

    public static final String VENDOR_SERVICE = "vendorService";

    /**
     * 采集模型id
     */
    public static final String MODEL_ID = "modelId";

    /**
     * 清洗模型id
     */
    public static final String TRANSFORM_ID = "transformId";

    /**
     * 挖掘模型id
     */
    public static final String DATAMINING_ID = "dataminingId";

    /**
     * 告警模型id
     */
    public static final String DECISION_ID = "decisionId";

    public static final String IS_BASE_TRUE = "yes";

    public static final String FILED_TYPE = "common";

    public static final String INSTANCE_CONNECTIONS = "connections";

    public static final String INSTANCE_PROPERTIES = "props";

    public static final String RES_TYPE_NAME = "resTypeName";

    public static final String RESFULL_TYPE = "resfullType";

    public static final String RESFULL_TYPE_NAME = "resfullTypeName";

    public static final String INSTANCE_MONITOR = "isMonitor";

    public static final String INSTANCE_REL_TARGET = "target";

    public static final String INSTANCE_LINK_SRC = "leftNodeId";

    public static final String INSTANCE_LINK_DEST = "rightNodeId";

    public static final String INSTANCE_REL_NAME = "relation";

    public static final String INSTANCE_MONITOR_DEFAULT = "false";

    public static final String INSTANCE_MONITOR_TRUE = "true";

    public static final String INSTANCE_NODE_ID = "ntmId";

    public static final String INSTANCE_LEVEL = "instanceLevel";

    public static final String ISBACKBONE = "isBackBone";

    public static final String EXTEND_PROPERTIES = "extendProperties";

    //private static final String REGEX = "(?<=\\$\\{)(.+?)(?=\\})";

    private static String cmdSourceExp = "\\$\\{(.*?)\\}(\\.\\$\\{(.*?)\\})+";

    private static Pattern cmdSourcePattern = Pattern.compile(cmdSourceExp);

    private static String cmdSingleSourceExp = "\\$\\{(.*?)\\}";

    private static Pattern cmdSingleSourcePattern = Pattern.compile(cmdSingleSourceExp);

    private static String formatSourceExp = "^[a-zA-Z]+\\(";

    private static Pattern formatSourcePattern = Pattern.compile(formatSourceExp);

    private static String upperresSourceExp = "\\$\\{UPPERRES\\.(.*?)\\}";

    private static Pattern upperresSourcePattern = Pattern.compile(upperresSourceExp);

    private static String cmdConnInfoSourceExp = "\\$\\{conninfo.(.*?)\\}";

    private static Pattern cmdConnInfoSourcePattern = Pattern.compile(cmdConnInfoSourceExp);

    private static String cmdPropInfoSourceExp = "\\$\\{propinfo.(.*?)\\}";

    private static Pattern cmdPropInfoSourcePattern = Pattern.compile(cmdPropInfoSourceExp);

    private static String methodExp = "\"((.+)\\((.*?)\\))\"";

    private static Pattern methodExpPattern = Pattern.compile(methodExp);

    private static final String POINT = "[.]";

    private static final String REAL_POINT = ".";

    private static final String LEFT_RANGE_SPLIT = "\\[";

    private static final String LEFT_RANGE = "[";

    private static final String LEFT_REPLACED_START = "${#";

    private static final String RIGHT_REPLACED_END = "}";


    /**
     * 该方法用于抽取生成规则中的变量
     * 匹配SpEl变量 如${A.values[0]}或者${A.values}
     *
     * @return
     */
    public static List<String> extractionParams(String format) {
        Matcher matcher = cmdSingleSourcePattern.matcher(format);
        List<String> params = new ArrayList<>();
        while (matcher.find()) {
            String param = matcher.group(1);
            params.add(param);
        }
        return params;
    }

    /**
     * 该方法用于抽取命令中相关key值，排重操作
     *
     * @param cmd
     * @return
     */
    public static List<String> extractionCmds(String cmd) {
        List<String> keys = new ArrayList<>();
        Matcher dsMatcher = cmdSourcePattern.matcher(cmd);
        while (dsMatcher.find()) {
            String key = dsMatcher.group(1);
            keys.add(key);
        }
        //		return keys.stream().map(String::toLowerCase).distinct().collect(Collectors.toList());
        return keys.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 该方法用于获取Cmd中连接信息变量字符串和对应连接信息的key值
     *
     * @return
     */
    public static Map<String, String> getConnectParam(String cmd) {
        Map<String, String> result = new HashMap<String, String>();
        Matcher connMatcher = cmdConnInfoSourcePattern.matcher(cmd);
        while (connMatcher.find()) {
            result.put(connMatcher.group(), connMatcher.group(1));
        }
        return result;
    }

    /**
     * 该方法用于获取Cmd中属性信息变量字符串和对应连接信息的key值
     *
     * @return
     */
    public static Map<String, String> getPropertyParam(String cmd) {
        Map<String, String> result = new HashMap<String, String>();
        Matcher connMatcher = cmdPropInfoSourcePattern.matcher(cmd);
        while (connMatcher.find()) {
            result.put(connMatcher.group(), connMatcher.group(1));
        }
        return result;
    }

}
