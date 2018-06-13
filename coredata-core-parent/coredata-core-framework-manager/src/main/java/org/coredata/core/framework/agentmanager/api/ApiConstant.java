package org.coredata.core.framework.agentmanager.api;

/**
 * 保存一些api中使用的常量信息
 * @author sushi
 *
 */
public interface ApiConstant {

	/**
	 * 成功常量
	 */
	public static final String SUCCESS = "success";

	/**
	 * 失败常量
	 */
	public static final String FAIL = "fail";

	/**
	 * 请求超时
	 */
	public static final String TIME_OUT = "timeout";

	/**
	 * 表明此次响应的返回状态
	 */
	public static final String STATUS = "status";

	/**
	 * 返回的结果集
	 */
	public static final String RESULTS = "results";

	/**
	 * 协议信息
	 */
	public static final String PROTOCOL = "protocol";

	/**
	 * snmp协议
	 */
	public static final String SNMP_PROTOCOL = "snmp";

	/**
	 * 被动接收协议
	 */
	public static final String RECEIVING_PROTOCOL = "receiving";

	/**
	 * 实例id集合
	 */
	public static final String INSTANCEID = "instanceId";

	/**
	 * agent采集明细内容
	 */
	public static final String DETAILS = "details";

	/**
	 * 用于不同协议获取Agent时，无对应对象的返回值
	 */
	public static final String NO_AGENT = "当前无可用Agent";

	/**
	 * 表明成功标识
	 */
	public static final String SUCCESS_FLAG = "1";

	/**
	 * 表明失败标识
	 */
	public static final String FAIL_FLAG = "0";

	/**
	 * 表明当前无可用的agent
	 */
	public static final String NO_AGENT_FLAG = "2";

	/**
	 * 表明请求超时的flag
	 */
	public static final String TIMEOUT_FLAG = "3";

	/**
	 * 表明协议之中可能存在的下划线
	 */
	public static final String CONNECT = "_";

	/**
	 * agent在线状态
	 */
	public static final int ON_LINE = 1;

	/**
	 * agent离线状态
	 */
	public static final int OFF_LINE = 0;

	/**
	 * 表明cmd运行的命令
	 */
	public static final String CMD = "cmd";

	/**
	 * 获取对应命令所属类型，root|multiple
	 */
	public static final String TYPE = "type";

	public static final String INSTANCE_ROOT_TYPE = "root";

	public static final String INSTANCE_LINK_DEVICE = "linkdevice";
}
