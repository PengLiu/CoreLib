package org.coredata.core.framework.agentmanager.api.client;

/**
 * 保存一些api中使用的常量信息
 * @author sushi
 *
 */
public interface ClientConstant {

	/**
	 * Instance_Manager请求context
	 */
	public static final String INSTANCE_CONTEXT = "InstanceContext";

	/**
	 * Core_Manager请求context
	 */
	public static final String CORE_CONTEXT = "CoreContext";

	/**
	 * 表明响应成功
	 */
	public static final String RESPONSE_SUCCESS_FLAG = "1";

	/**
	 * GET方法常量
	 */
	public static final String GET = "GET";

	/**
	 * POST方法常量
	 */
	public static final String POST = "POST";

	/**
	 * 路径拆分标识
	 */
	public static final String SPLIT = "/";

	/**
	 * 默认编码
	 */
	public static final String ENCODING = "UTF-8";

	/**
	 * 设置contenttype
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * json的content type
	 */
	public static final String CONTENT_TYPE_JSON = "application/json";

	/**
	 * 表明采集回来的记录是否保留表头
	 */
	public static final String WITH_HEADER = "withheader";

	/**
	 * 表明采集命令的名称
	 */
	public static final String CMD_NAME = "name";

	/**
	 * 表明实例对象中的连接信息名称
	 */
	public static final String CONNECTIONS = "connections";

	/**
	 * 表明返回值中null字符串
	 */
	public static final String NULL_VALUE = "null";

}
