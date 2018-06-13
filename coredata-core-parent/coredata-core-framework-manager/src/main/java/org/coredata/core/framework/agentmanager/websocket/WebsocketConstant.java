package org.coredata.core.framework.agentmanager.websocket;

/**
 * websocket常量接口
 * @author sushi
 *
 */
public interface WebsocketConstant {

	/**
	 * 表明Agent动作
	 */
	public static final String ACTION = "action";

	/**
	 * Agent的登录动作
	 */
	public static final String ACTION_LOGIN = "login";

	/**
	 * 上报Tasks数
	 */
	public static final String ACTION_TASK_REPORT = "tasks";

	/**
	 * Agent的ping动作
	 */
	public static final String ACTION_PING = "ping";

	/**
	 * 下发给Agent的测试动作，同时返回的test响应
	 */
	public static final String ACTION_TEST = "test";

	/**
	 * 下发给Agent的检测动作，同时返回的detect响应
	 */
	public static final String ACTION_DETECT = "detect";

	/**
	 * 删除Agent正在运行的采集任务
	 */
	public static final String ACTION_DELETE = "delete";

	/**
	 * 下发给Agent的实例化动作，同时返回的instance响应
	 */
	public static final String ACTION_INSTANCE = "instance";

	/**
	 * 下发给Agent的临时采集动作，同时返回的temporary响应
	 */
	public static final String ACTION_TEMPORARY = "temporary";

	/**
	 * 下发给Agent的采集动作，同时返回的collect响应
	 */
	public static final String ACTION_COLLECT = "collect";

	/**
	 * 下发给Agent的set动作
	 */
	public static final String ACTION_SNMP_SET = "snmp_set";

	/**
	 * 下发给Agent的操作动作，同时返回的action响应
	 */
	public static final String ACTION_ACTION = "action";

	public static final String ACTION_REALTIME_COLLECT = "realtime_collect";

	public static final String BUSINESS_INTEGRATION = "BusinessIntegration";

	/**
	 * 下发给Agent的健康检查动作，同时返回的healthcheck响应
	 */
	public static final String ACTION_HEALTHCHECK = "healthcheck";

	/**
	 * 下发给Agent的被动接收命令
	 */
	public static final String ACTION_RECEIVING = "receiving";

	/**
	 * 下发给Agent的启动服务命令
	 */
	public static final String ACTION_SERVER = "server";

	/**
	 * Agent返回的seq序列
	 */
	public static final String SEQ = "seq";

	/**
	 * Angent响应返回时携带的result字段，由此判定是否为Agent响应
	 */
	public static final String AGENT_RESULT = "result";

	/**
	 * 请求时携带的connections信息
	 */
	public static final String REQUEST_CONNECTION = "connections";

	/**
	 * 表明服务返回响应成功
	 */
	public static final String RESPONSE_OK = "1";

	/**
	 * 表明服务器返回的响应失败，系统内部错误
	 */
	public static final String RESPONSE_SYS_ERR = "4";

	/**
	 * 表明服务器返回的响应失败，命令格式错误
	 */
	public static final String RESPONSE_CMD_ERR = "5";

	/**
	 * 表明服务器返回的响应失败，证书信息验证失败
	 */
	public static final String RESPONSE_CREDENTIALS_ERR = "2";

	/**
	 * 表明服务器返回的响应失败，超出可连接的最大Agent数
	 */
	public static final String RESPONSE_AGENTNUM_ERR = "3";

	/**
	 * 平台可添加的总共agent个数
	 */
	public static final int TOTAL_AGENT_COUNT = 100;

	/**
	 * 启动服务的端口
	 */
	public static final String SERVER_PORT = "port";

	/**
	 * 启动的服务名称
	 */
	public static final String SERVER_NAME = "serverName";

	/**
	 * 开启/关闭服务的标识
	 */
	public static final String SERVER_STATUS = "serverStatus";

	/**
	 * 返回的消息内容
	 */
	public static final String RESULT_MSG = "msg";

	/**
	 * 同步不支持命令
	 */
	public static final String ACTION_SYS_CMDS = "syscmds";

	/**
	 * 不支持命令的资产id
	 */
	public static final String INST_ID = "instId";

	/**
	 * 不支持命令集合
	 */
	public static final String NOT_SUPPORT_CMDS = "cmds";
	
	/**
	 * 卓智智能插座动作
	 */
	public static final String ACTION_ZZOUTLETTCP = "ZzOutletTcp";

}
