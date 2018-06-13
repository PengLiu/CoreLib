package org.coredata.core.framework.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coredata.library.obj.ISInfo;
import com.coredata.ntm.api.NNLinkResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coredata.core.framework.action.processer.IotgatewayActionProcesser;
import org.coredata.core.framework.action.processer.SmartisysActionProcesser;
import org.coredata.core.framework.agentmanager.entity.Agent;
import org.coredata.core.framework.agentmanager.service.AgentManagerService;
import org.coredata.core.framework.agentmanager.service.AgentTaskService;
import org.coredata.core.framework.agentmanager.util.LogUtil;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;
import org.coredata.core.model.constants.ApiConstant;
import org.coredata.core.model.constants.ClientConstant;
import org.coredata.core.model.discovery.DiscoveryModel;
import org.coredata.core.model.discovery.Instance;
import org.coredata.core.model.entity.ConnectionInfo;
import org.coredata.core.model.service.ActionService;
import org.coredata.core.model.service.CollectionService;
import org.coredata.core.model.service.DiscoveryModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类用于调用Agent_Manager相关方法service接口实现类
 * @author sushi
 *
 */
@Service
public class AgentService {

	private static Logger logger = LoggerFactory.getLogger(AgentService.class);

	private static final String POINT = ",";

	private static final String WIRELESSAC_START = "wirelessac_";

	@Autowired
	private DiscoveryModelService discoveryService;

//	@Autowired
//	private CommonProcesser commonProcesser;

	@Autowired
	private IotgatewayActionProcesser iotgatewayActionProcesser;

	@Autowired
	private SmartisysActionProcesser smartisysActionProcesser;

//	@Autowired
//	private VmwareProcesser vmwareProcesser;

//	@Autowired
//	private WirelessProcesser wirelessProcesser;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private ActionService actionService;

	@Autowired
	private AgentTaskService agentTaskService;

	@Autowired
	private AgentManagerService agentManagerService;

//	@Autowired
//	private InstanceRestypeService instanceRestypeService;

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * kafka下发模型时主题topic
	 * @author sushi
	 *
	 */
	public enum TopicModels {
		TRANSFORM, DATAMINING
	}

	/**
	 * 该方法用于返回全部Agent信息
	 * @return
	 */
	
	public String findAllAgent() {
		List<Agent> agentList = agentManagerService.findAllAgent();
		String agents = JSON.toJSONString(agentList);
		//		String agents = agentClient.findAllAgent();

		JSONArray array = JSON.parseArray(agents);
		List<Map<String, Object>> results = new ArrayList<>();
		for (Object obj : array) {
			Map<String, Object> agentObj = new HashMap<>();
			StringBuilder displayName = new StringBuilder();
			JSONObject agent = (JSONObject) obj;
			displayName.append(agent.get("ipAddress").toString());
			String remarks = agent.get("remarks") == null ? "" : agent.get("remarks").toString();
			if (!StringUtils.isEmpty(remarks)) {
                displayName.append("（").append(remarks).append("）");
            }
			Long id = Long.parseLong(agent.get("id").toString());
			agentObj.put("id", id);
			agentObj.put("name", displayName);
			results.add(agentObj);
		}
		return JSON.toJSONString(results);
	}

	/**
	 * 该方法用于调用AgentManager的方法开始正式采集
	 * @return
	 */
	@SuppressWarnings("unchecked")
	
	public String createAgentCollection(String instanceId, String protocol) {

		String result = ClientConstant.RESPONSE_FAIL_FLAG;
		List<String> instances = getMonitorInstance(instanceId);

		if (instances.size() <= 0) {
			return result.toString();
		}

		Map<String, Object> params = new HashMap<>();
		collectionService.processCollectParams(params, instances, protocol);

		try {
			String cmd = mapper.writeValueAsString(params);

			try {
				result = agentManagerService.sendAgentCmd(cmd);
			} catch (Exception e) {
				logger.error(LogUtil.stackTraceToString(e));
				result = processExceptionResponse(cmd);
			}

			List<Map<String, String>> respstatus = JSON.parseObject(result, List.class);

			boolean success = true;
			for (Map<String, String> res : respstatus) {
				String status = res.get(ClientConstant.RESPONSE_RESULT);
				if (!ClientConstant.RESPONSE_SUCCESS_FLAG.equals(status)) {
					success = false;
					break;
				}
			}
			if (success) {
                result = ClientConstant.RESPONSE_SUCCESS_FLAG;
            }

		} catch (JsonProcessingException e) {

		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private String processExceptionResponse(String cmd) {
		//设置返回响应
		Map<String, Object> error = new HashMap<>();
		JSONObject json = JSON.parseObject(cmd);
		String seq = json.getString(WebsocketConstant.SEQ);
		error.put(WebsocketConstant.SEQ, seq);
		String connections = json.getString(WebsocketConstant.REQUEST_CONNECTION);
		List<Map<String, String>> connects = JSON.parseObject(connections, List.class);
		List<Map<String, String>> results = new ArrayList<>();
		connects.forEach(c -> {
			Map<String, String> result = new HashMap<>();
			result.put(ApiConstant.PROTOCOL, c.get(ApiConstant.PROTOCOL));
			result.put(ApiConstant.STATUS, ApiConstant.FAIL_FLAG);
			results.add(result);
		});
		error.put(ApiConstant.RESULTS, results);
		return JSON.toJSONString(error);
	}

	@SuppressWarnings("unchecked")
	private List<String> getMonitorInstance(String instanceId) {
		List<String> resultInstance = new ArrayList<>();
		//此处先根据根节点的instanceId获取对应根实例

//		List<InstanceRestype> tmp = instanceRestypeService.findMonitorInstancesByRootUnique(instanceId);
//		String allInstances = JSON.toJSONString(tmp);
//		//String allInstances = instanceClient.findMonitorInstancesByRootUnique(instanceId);
//		if (StringUtils.isEmpty(allInstances) || "null".equals(allInstances))
//			return resultInstance;
//		List<JSONObject> instances = JSON.parseObject(allInstances, List.class);
//		instances.forEach(j -> {
//			resultInstance.add(j.toString());
//		});
		return resultInstance;
	}

	/**
	 * 该方法用于调用AgentManager的实例化方法
	 * @param info
	 */
	
	public void createAgentInstance(String customerId, ConnectionInfo info) {
		//此处调整实例化方法至此处，agentClient只负责发送相关指令
//		String response = prepareInstance(customerId, info);
//		if (ClientConstant.RESPONSE_FAIL_FLAG.equals(response)) {
//			Map<String, String> resp = new HashMap<>();
//			resp.put(ClientConstant.SERVER_REQUEST_SEQ, info.getSeq());
//			resp.put(ClientConstant.STATUS, ClientConstant.RESPONSE_FAIL_FLAG);
//			InstanceUtil.sendInstanceResultToKafka(JSON.toJSONString(resp));
//		}
	}

	
	public void createAgentInstance(ConnectionInfo info) {
		createAgentInstance(null, info);
	}

	/**
	 * 该方法用于准备实例化之前的命令
	 * @param info
	 * @return
	 */
//	private String prepareInstance(String customerId, ConnectionInfo info) {
//		//此处针对switch，router等网络设备进行sysoid查询
//		DiscoveryModel discoveryModel = discoveryService.findInsDiscoveryModelId(info);
//		if (discoveryModel == null)
//			return ClientConstant.RESPONSE_FAIL_FLAG;
//		List<Instance> instance = discoveryModel.getInstance();//获取实例化集合
//
//		//绑定CustomerId和批量发现标识
//		if (!StringUtils.isEmpty(customerId)) {
//			for (Instance inst : instance) {
//				inst.setCustomerId(customerId);
//				inst.setBatchDiscover(info.isBatchDiscover());
//				inst.setExtendProperties(info.getExtendProperties());
//			}
//		}
//
//		if (CollectionUtils.isEmpty(instance))
//			return ClientConstant.RESPONSE_FAIL_FLAG;
//		if (discoveryModel.getId().startsWith(WIRELESSAC_START))
//			return wirelessProcesser.process(info, instance);
//		else if (discoveryModel.getId().startsWith("smartisys") || discoveryModel.getId().startsWith("kvm") || discoveryModel.getId().startsWith("vmware")
//				|| discoveryModel.getId().startsWith("xgmiot"))
//			return vmwareProcesser.process(info, instance);
//		else
//			return commonProcesser.process(info, instance);
//	}

	/**
	 * 该方法用于插入agentTask任务表数据
	 */
	
	public String createAgentTask(String instanceId) {
		List<Map<String, String>> agentTasks = new ArrayList<>();
		//此处需要调整代码逻辑，将可能任务拆分多条
		processAgentTask(instanceId, agentTasks);
		if (agentTasks.size() <= 0) {
			return ClientConstant.RESPONSE_SUCCESS;
		}
		return agentTaskService.insertAgentTask(agentTasks);
	}

	/**
	 * 该方法用于重新整理instanceId
	 * @param instanceId
	 * @param agentTasks
	 */
	private void processAgentTask(String instanceId, List<Map<String, String>> agentTasks) {

//		Map<String, List<InstanceRestype>> collectors = new HashMap<>();
//		String[] instances = instanceId.split(POINT);
//		Set<String> allPro = new HashSet<>();
//		for (String ins : instances) {
//
//			InstanceRestype instanceRestype = instanceRestypeService.findByUniqueIdent(ins);
//			if (instanceRestype == null) {
//				continue;
//			}
//			if (logger.isDebugEnabled())
//				logger.debug("------Create Agent Task Instance is ::: " + ins + "---entity:::" + instanceRestype);
//			//此处开始获取是否需要多协议进行
//			String modelId = instanceRestype.getModelId();
//			String resType = instanceRestype.getResType();
//			CollectionModel model = collectionService.findById(modelId);//获取对应的采集模型
//			if (model == null) {
//				logger.error("------CollectionModel " + modelId + " is not exist");
//				continue;
//			}
//			List<Collector> colCmds = model.getCollector();//获取采集模型中的命令
//			for (Collector colCmd : colCmds) {
//				String type = colCmd.getType();//获取命令对应的协议
//				if (StringUtils.isEmpty(type))
//					continue;
//				allPro.add(type);
//			}
//			String type = instanceRestype.getNodeLevel();// json.get(ClientConstant.NODELEVEL).toString();//获取对应资源类型
//			String collectorKey = ins;
//			if (!ClientConstant.TYPE_ROOT.equals(type) && !ClientConstant.LINK_RESTYPE.equals(resType)) //如果不是根资源，跳过此次循环
//				continue;
//			List<InstanceRestype> collector = collectors.get(collectorKey);
//			if (collector == null)
//				collector = new ArrayList<>();
//			collector.add(instanceRestype);
//			collectors.put(collectorKey, collector);
//		}
//		collectors.forEach((k, v) -> {
//			List<Map<String, String>> param = joinInstanceId(v, allPro);
//			if (param.size() <= 0)
//				return;
//			agentTasks.addAll(param);
//		});
	}

	/**
	 * 该方法用于根据实例集合，拼接结果
	 * @param
	 * @return
	 */
	@SuppressWarnings("unchecked")
//	private List<Map<String, String>> joinInstanceId(List<InstanceRestype> instances, Set<String> allPro) {
//		List<Map<String, String>> params = new ArrayList<>();
//		List<Map<String, String>> conns = new ArrayList<>();
//		StringBuilder instanceId = new StringBuilder();
//		for (InstanceRestype ins : instances) {
//			String type = ins.getNodeLevel();//   ins.get(ClientConstant.NODELEVEL).toString();//获取对应资源类型
//			String resType = ins.getResType();//ins.get(ClientConstant.INSTANCE_RESTYPE).toString();//获取对应资产类型
//			String connections = ins.getConnections();
//			String instId = ins.getInstId();
//			if (ClientConstant.TYPE_ROOT.equals(type) || ClientConstant.LINK_RESTYPE.equals(resType))//如果是根节点或者链路节点，拼接对应协议
//				conns = JSON.parseObject(connections, List.class);
//			instanceId.append(POINT).append(instId);
//		}
//		if (instanceId.length() <= 0 || conns.size() <= 0)
//			return params;
//		for (Map<String, String> c : conns) {
//			String protocol = c.get(ClientConstant.PROTOCOL);
//			if (allPro.contains(protocol))
//				allPro.remove(protocol);
//			Map<String, String> param = new HashMap<>();
//			param.put(ClientConstant.INSTANCERESTYPE_ID, instanceId.substring(1).toString());
//			param.put(ClientConstant.PROTOCOL, protocol);
//			params.add(param);
//		}
//		if (allPro.size() > 0) {
//			allPro.forEach(pro -> {
//				Map<String, String> param = new HashMap<>();
//				param.put(ClientConstant.INSTANCERESTYPE_ID, instanceId.substring(1).toString());
//				param.put(ClientConstant.PROTOCOL, pro);
//				params.add(param);
//			});
//		}
//		return params;
//	}

	
	public int monitor(String instId, boolean monitor) {
		try {
			if (!monitor) {
				return agentTaskService.cancelTask(instId);
			} else {
				return agentTaskService.addMonitor(instId);
			}
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 该方法用于创建链路实例化
	 * @param info
	 * @param link
	 */
	@SuppressWarnings("unchecked")
	
	public void createLinkInstance(ConnectionInfo info, List<NNLinkResource> link) {
		String discoverId = info.getDiscoverId();//获取对应资源类型id，此处应该为link
		DiscoveryModel discoveryModel = discoveryService.findById(discoverId);
//		List<Instance> instances = discoveryModel.getInstance();
//		if (CollectionUtils.isEmpty(instances))
//			return;
//		Map<String, Object> params = new HashMap<>();
//		commonProcesser.processInstanceParams(params, info, instances);//拼接相关参数
//		List<Map<String, String>> preCmds = (List<Map<String, String>>) params.get(ClientConstant.INSTANCE_PRE_CMDS);
//		Map<String, List<Object>> dataSource = new HashMap<>();//保存预置命令数据源
//		link.forEach(l -> {
//			preCmds.forEach(pcmd -> {
//				Map<String, Object> env = new HashMap<>();//执行命令整体环境参数
//				String cmd = pcmd.get(ClientConstant.FORMAT);
//				//根据预置命令，获取对应值
//				List<ISInfo> iss = InstanceUtil.extractionInstance(cmd);//拆分相关变量及id信息
//				bindPreCmdForLink(l, iss);
//				env.put(ClientConstant.IS_INFO, iss);
//				Object returnValue = FunctionUtil.executeFunction(cmd, env);
//				String key = pcmd.get(ClientConstant.TEST_NAME);
//				List<Object> values = dataSource.get(key);
//				if (values == null)
//					values = new ArrayList<>();
//				values.add(returnValue);
//				dataSource.put(key, values);
//			});
//		});
		//处理相关预置参数后，重新更新相关命令
//		processLinkInstance(params, dataSource, link, instances);
	}

	/**
	 * 该方法用于真正进行链路资源发现方法
	 * @param params
	 * @param dataSource
	 */
	@SuppressWarnings("unchecked")
	private void processLinkInstance(Map<String, Object> params, Map<String, List<Object>> dataSource, List<NNLinkResource> link, List<Instance> instances) {
//		List<Map<String, String>> insts = (List<Map<String, String>>) params.get(ClientConstant.SERVER_REQUEST_ACTION_INSTANCE);
//		List<Map<String, String>> replaceInsts = new ArrayList<>();
//		for (Map<String, String> inst : insts) {
//			String[] joinCmd = null;
//			String cmd = inst.get(ClientConstant.CMD);//获取对应cmd值
//			Set<Entry<String, List<Object>>> entrySet = dataSource.entrySet();
//			for (Entry<String, List<Object>> entry : entrySet) {//更新命令中相关变量
//				List<Object> value = entry.getValue();
//				if (joinCmd == null) {
//					joinCmd = new String[value.size()];
//					for (int i = 0; i < joinCmd.length; i++)
//						joinCmd[i] = cmd.replace("${" + entry.getKey().toUpperCase() + "}", value.get(i).toString());
//				} else {
//					for (int i = 0; i < joinCmd.length; i++)
//						joinCmd[i] = joinCmd[i].replace("${" + entry.getKey().toUpperCase() + "}", value.get(i).toString());
//				}
//			}
//			inst.put(ClientConstant.CMD, StringUtil.join(joinCmd));
//			replaceInsts.add(inst);
//		}
//		//更新后实例化相关资源
//		Map<String, Object> preparams = new HashMap<>();
//		preparams.putAll(params);
//		preparams.remove(ClientConstant.INSTANCE_PRE_CMDS);
//		preparams.put(ClientConstant.SERVER_REQUEST_ACTION_INSTANCE, replaceInsts);
//		String saveConnect = params.get(ClientConstant.SERVER_REQUEST_CONNECT).toString();
//		LinkCallback lCallback = new LinkCallback(instanceRestypeService, this);
//		lCallback.getParams().setLinks(link);
//		lCallback.getParams().setSaveConnect(saveConnect);
//		lCallback.getParams().setInstance(instances);
//		agentManagerService.getInstCmdResult(preparams, lCallback);
	}

	/**
	 * 为链路绑定相关资源
	 */
	@SuppressWarnings("unchecked")
	private void bindPreCmdForLink(NNLinkResource link, List<ISInfo> iss) {
		String linkStr = JSON.toJSONString(link);//将link对象转换为json字符串
		Map<String, Object> linksrc = JSON.parseObject(linkStr, Map.class);
		iss.forEach(is -> {
			String key = is.getKey();
			Object value = linksrc.get(key);
			is.setValue(value);
		});
	}

	
	public void removeTasks(String instId) {
		agentTaskService.removeTasks(instId);
		//		agentClient.removeTasks(instId);
	}

//	private String findInstanceByUid(String uid) {
//		InstanceRestype instanceRestype = instanceRestypeService.findByUniqueIdent(uid);
//		String instanceJson = JSON.toJSONString(instanceRestype);
//		return instanceJson;
//	}


	
//	public String createAgentAction(String instanceId, String actionId, String controllerId, String seq) {
//		String result = ClientConstant.RESPONSE_FAIL_FLAG;
//		;
//		try {
//			//String instanceStr = instanceClient.findInstanceByUniqueIdent(instanceId);
//			String instanceStr = findInstanceByUid(instanceId);
//			JsonNode instance = mapper.readTree(instanceStr);
//
//			ActionModel actionModel = actionService.findActionModelByModelId(instance.get(ClientConstant.MODEL_INS_ID).asText());
//			Action action = actionModel.getAction().stream().filter(s -> actionId.equals(s.getId())).findFirst().get();
//			Controller controller = action.getController().stream().filter(s -> controllerId.equals(s.getId())).findFirst().get();
//
//			String protocol = action.getType();
//			String rootInstanceStr = findInstanceByUid(instanceId);
//			//String rootInstanceStr = instanceClient.findInstanceByUniqueIdent(rootUnique);
//
//			JsonNode rootInstance = mapper.readTree(rootInstanceStr);
//			List<Map<String, String>> allConns = JSON.parseObject(rootInstance.get("connections").asText(), List.class);
//			Map<String, String> connectInfo = allConns.stream().filter(c -> c.get(ClientConstant.PROTOCOL).equals(protocol)).findFirst().get();
//			Map<String, Object> params = new HashMap<>();
//			prepareAction(params, instance, controller, connectInfo);
//
//			//TODO:异步等待
//			result = agentManagerService.sendAgentCmd(JSON.toJSONString(params));
//
//			//result = agentClient.agentAction(params);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ClientConstant.RESPONSE_FAIL_FLAG;
//		}
//		return result;
//	}

	/**
	 * 该方法用于准备Action命令
	 */
//	private void prepareAction(Map<String, Object> params, JsonNode instance, Controller controller, Map<String, String> connectInfo) {
//
//		switch (connectInfo.get("protocol")) {
//		case "iotgateway":
//			iotgatewayActionProcesser.processActionParams(params, instance, controller, connectInfo);
//			break;
//		case "smartisys_http":
//			smartisysActionProcesser.processActionParams(params, instance, controller, connectInfo);
//			break;
//		default:
//			break;
//		}
//	}

}