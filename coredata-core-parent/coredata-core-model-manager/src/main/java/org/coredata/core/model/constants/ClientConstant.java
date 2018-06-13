package org.coredata.core.model.constants;

/**
 * 保存一些api中使用的常量信息
 * @author sushi
 *
 */
public interface ClientConstant {

	/**
	 * Agent_Manager请求context
	 */
	public static final String AGENT_CONTEXT = "AgentContext";

	/**
	 * Transform_Manager请求context
	 */
	public static final String TRANSFORM_CONTEXT = "TransformContext";

	/**
	 * topo发现请求的context
	 */
	public static final String TOPO_CONTEXT = "TopoContext";

	/**
	 * Instance_Manager请求context
	 */
	public static final String INSTANCE_CONTEXT = "InstanceContext";

	/**
	 * GET方法常量
	 */
	public static final String GET = "GET";

	public static final String DELETE = "DELETE";

	/**
	 * POST方法常量
	 */
	public static final String POST = "POST";

	/**
	 * 服务响应返回值-成功
	 */
	public static final String RESPONSE_SUCCESS = "success";

	/**
	 * 服务响应返回值-失败
	 */
	public static final String RESPONSE_FAIL = "fail";

	/**
	 * 服务响应失败数字结果
	 */
	public static final String RESPONSE_FAIL_FLAG = "0";

	/**
	 * 服务响应成功数字结果
	 */
	public static final String RESPONSE_SUCCESS_FLAG = "1";

	/**
	 * 服务响应无实例化模型结果
	 */
	public static final String RESPONSE_NO_INSTANCE_FAIL = "4";

	/**
	 * 服务响应无可用Agent结果
	 */
	public static final String RESPONSE_NO_AGENT_FLAG = "2";

	/**
	 * 服务响应超时结果
	 */
	public static final String RESPONSE_TIME_OUT_FLAG = "3";

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
	 * 发送请求的action变量key
	 */
	public static final String SERVER_REQUEST_ACTION = "action";

	/**
	 * 发送请求的action变量test
	 */
	public static final String SERVER_REQUEST_ACTION_TEST = "test";

	/**
	 * 发送请求的action变量instance
	 */
	public static final String SERVER_REQUEST_ACTION_INSTANCE = "instance";

	/**
	 * 发送请求的action变量collect
	 */
	public static final String SERVER_REQUEST_ACTION_COLLECT = "collect";
	
	public static final String SERVER_REQUEST_ACTION_REALTIME_COLLECT = "realtime_collect";

	/**
	 * 发送请求的action变量action
	 */
	public static final String SERVER_REQUEST_ACTION_ACTION = "action";

	/**
	 * 发送请求的action变量手动实例
	 */
	public static final String SERVER_REQUEST_ACTION_INSTANCE_MANUAL = "manualinstance";

	/**
	 * 发送请求的序列号，用于标注是哪次请求
	 */
	public static final String SERVER_REQUEST_SEQ = "seq";

	/**
	 * 发送请求的connect变量key
	 */
	public static final String SERVER_REQUEST_CONNECT = "connections";

	/**
	 * 发送请求的测试单元变量key
	 */
	public static final String SERVER_REQUEST_TEST_REQUIRED = "prerequired";

	/**
	 * 发送请求的kafka主题key
	 */
	public static final String KAFKA_TOPIC = "kafkaTopic";

	/**
	 * 发送请求的kafka地址key
	 */
	public static final String KAFKA_ADDRESS = "kafkaAddress";

	/**
	 * 连接信息返回的格式
	 */
	public static final String CONNECTION_RESULT = "json";

	/**
	 * 连接信息所用到协议
	 */
	public static final String CONNECTION_TYPE = "conninfo";

	/**
	 * 数据库类型key值
	 */
	public static final String CONNECTION_DBTYPE = "dbtype";

	/**
	 * 连接信息下划线
	 */
	public static final String CONNECT = "_";

	/**
	 * 表明测试命令
	 */
	public static final String CMD = "cmd";

	/**
	 * 表明测试所依赖的协议
	 */
	public static final String PROTOCOL = "protocol";

	/**
	 * 表明是否需要保留表头
	 */
	public static final String WITH_HEADER = "withheader";

	/**
	 * 表明测试项名称
	 */
	public static final String TEST_NAME = "name";

	/**
	 * 表明断言方式
	 */
	public static final String TEST_ASSERT_TYPE = "asserttype";

	/**
	 * 表明测试期望值
	 */
	public static final String TEST_EXPECTED = "expected";

	/**
	 * 表明Agent响应的返回值内容
	 */
	public static final String AGENT_RESPONSE_RESULTS = "results";

	/**
	 * 表明连接信息中的超时时间
	 */
	public static final String TIME_OUT = "timeout";

	/**
	 * 表明采集命令重试次数
	 */
	public static final String RETRY = "retry";

	/**
	 * 表明命令采集周期
	 */
	public static final String PERIOD = "period";

	/**
	 * 表明采集命令
	 */
	public static final String COLLECT_ID = "id";

	/**
	 * 表明实例化对象id
	 */
	public static final String INSTANCE_ID = "instanceid";
	
	public static final String INST_ID = "instId";

	/**
	 * 表明资源类型id
	 */
	public static final String RESTYPE = "restype";

	/**
	 * 表明发现后资产资源类型id
	 */
	public static final String INSTANCE_RESTYPE = "resType";

	/**
	 * 表明模型类型id
	 */
	public static final String MODEL_INS_ID = "modelId";
	
	//清洗模型ID
	public static final String MODEL_INS_ID_T = "modelTId";
	public static final String MODEL_INS_ID_ST = "transformId";
	//挖掘模型ID
	public static final String MODEL_INS_ID_A = "modelAId";
	public static final String MODEL_INS_ID_SA = "dataminingId";
	//决策模型ID
	public static final String MODEL_INS_ID_D = "modelDId";
	public static final String MODEL_INS_ID_SD = "decisionId";
	

	/**
	 * 表明采集命令中的参数列表
	 */
	public static final String PARAMS = "params";
	/**
	 * 表明采集命令中的参数列表(针对模型中而言)
	 */
	public static final String PARAMS_MAP = "paramsMap";

	/**
	 * 表明实例化资源中type类型
	 */
	public static final String NODELEVEL = "nodeLevel";

	/**
	 * 表明实例化资源中type类型的根
	 */
	public static final String TYPE_ROOT = "root";

	/**
	 * 表明采集命令节点
	 */
	public static final String COLLECTOR = "collector";

	/**
	 * 表明动作命令节点
	 */
	public static final String CONTROLLER = "controller";

	/**
	 * 表明实例化对象中instanceId属性
	 */
	public static final String INSTANCERESTYPE_ID = "instanceId";

	/**
	 * 表明模型id
	 */
	public static final String MODEL_ID = "modelid";

	/**
	 * 表明返回值状态
	 */
	public static final String STATUS = "status";

	/**
	 * 表明实例化资源的根节点对应唯一标识
	 */
	public static final String ROOT_INSTID = "rootInstId";

	/**
	 * 表明响应结果标识
	 */
	public static final String RESPONSE_RESULT = "result";

	/**
	 * 表明实例化资源的关系
	 */
	public static final String INSTANCE_RELATION = "relation";

	/**
	 * 表明实例化的下级节点
	 */
	public static final String INSTANCE_RELATION_END_NODE = "endNode";

	/**
	 * 表明是否加入监控
	 */
	public static final String INSTANCE_IS_MONITOR = "isMonitor";

	/**
	 * 表明方法名称，执行cmd后的封装方法
	 */
	public static final String METHOD = "method";

	/**
	 * 表明获取对应值的format方式
	 */
	public static final String FORMAT = "format";

	/**
	 * 表明预处理命令中的cmd的name值
	 */
	public static final String CMD_KEY = "cmdKey";

	/**
	 * 表明实例化前的预置命令
	 */
	public static final String INSTANCE_PRE_CMDS = "precmds";

	/**
	 * 表明实例化参数信息
	 */
	public static final String IS_INFO = "Instance_info";

	/**
	 * 表明属性参数信息
	 */
	public static final String PROPERTY_INFO = "Property_info";

	/**
	 * 表明数据源类型
	 */
	public static final String DATA_SOURCE = "Data_source_info";

	/**
	 * 表明拓扑发现传来的节点id
	 */
	public static final String NODE_ID = "nodeId";

	/**
	 * 实例化资源处理类型，分为old，new两种
	 */
	public static final String INSTANCE_PROCESS_TYPE = "processType";

	/**
	 * 实例化资源表明第二批预处理命令
	 */
	public static final String SECOND_CMDS = "secondPreCmd";
	/**
	 * 表明指令结果是否全局使用
	 */
	public static final String CMD_IS_GLOBALRESULT = "isGlobalResult";
	/**
	 * 表明指令是否可用性数据指令
	 */
	public static final String CMD_IS_AVAILCMD = "isavailcmd";
	/**
	 * 表明指令是否可用（加入监控）
	 */
	public static final String CMD_ENABLE = "enable";
	/**
	 * root资源实例对应的加入监控的子实例信息
	 */
	public static final String CMD_SUB_INSTANCE_INFO = "subInstanceInfo";
	/**
	 * 实例化资源的属性
	 */
	public static final String INSTANCE_PROPERTIES = "props";
	/**
	 * 实例化资源的属性中索引
	 */
	public static final String INSTANCE_PROPERTIES_INDEX = "index";

	/**
	 * 链路资源类型
	 */
	public static final String LINK_RESTYPE = "linkdevice";

	/**
	 *  输出给前端的view变量
	 */
	public static final String VIEW = "view";

}
