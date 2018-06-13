package org.coredata.core.framework.agentmanager.service;

import com.coredata.utils.security.MD5;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.coredata.core.framework.agentmanager.cmds.CallFuture;
import org.coredata.core.framework.agentmanager.cmds.Command;
import org.coredata.core.framework.agentmanager.config.AgentManagerConfig;
import org.coredata.core.framework.agentmanager.entity.Agent;
import org.coredata.core.framework.agentmanager.entity.AgentTask;
import org.coredata.core.framework.agentmanager.repository.AgentRepository;
import org.coredata.core.framework.agentmanager.repository.AgentTaskRepository;
import org.coredata.core.framework.agentmanager.util.DatetimeUtil;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;
import org.coredata.core.framework.agentmanager.websocket.WebsocketUtil;
import org.coredata.core.model.constants.ApiConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@Transactional
public class AgentTaskService  {

	private static final ExecutorService executor = Executors.newFixedThreadPool(5);

	private static final String POINT = ",";

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private AgentTaskRepository agentTaskRepository;

	@Autowired
	private AgentRepository agentRepository;

	@Autowired
	private AgentManagerConfig config;

	private boolean needRetry = false;

	private int retryTime = 10;

	@PostConstruct
	public void initRetry() {
		this.needRetry = config.isNeedRetry();
		this.retryTime = config.getRetryTime();
	}

	
	public int updateAgentSignature(String ids) {
		int updateAgentSignature = agentTaskRepository.updateAgentSignature(ids);
		return updateAgentSignature;
	}

	/**
	 * 该方法用于分配任务列表中任务
	 */
	
	public void assignmentsTask() {
		//每次处理是任务表中全部任务
		String ids = agentTaskRepository.getAllNoAgentTaskIds(needRetry, retryTime);
		//如果没有待分配的任务，则直接返回
		if (ids == null)
			return;
		//如果没有被更新的任务，则直接返回，表明该实例没有需要处理的任务
		int result = updateAgentSignature(ids);
		if (result == 0)
			return;
		//根据ids集合循环处理这些记录
//		realAssignmentsTask(ids);
	}

	
	public int addMonitor(String instId) {
		return agentTaskRepository.addMonitor(instId);
	}

	
	public int cancelTask(String instId) {

		//get instTasks
		List<AgentTask> tasks = agentTaskRepository.findInstTasks(instId);
		if (CollectionUtils.isEmpty(tasks)) {
			return 0;
		}

		//send stop message to agent
		tasks.forEach(task -> {
			if (task.getStatus() == 1) {
				//监控中
				long agentId = task.getAgentId();
				String taskId = MD5.getMD5(MD5.getMD5(instId) + task.getProtocol());
				Agent agent = agentRepository.findAgentById(agentId);
				if (agent != null) {
					try {
						String seq = UUID.randomUUID().toString();
						ObjectNode cmd = mapper.createObjectNode();
						cmd.put(WebsocketConstant.ACTION, WebsocketConstant.ACTION_DELETE);
						cmd.put(WebsocketConstant.SEQ, seq);
						ArrayNode taskArray = mapper.createArrayNode();
						taskArray.add(taskId);
						cmd.set("tasks", taskArray);
						//TODO:CallFuture 对于超时的CallFuture从队列中清除掉
						CallFuture future = new CallFuture(seq, 60000);
						Command.futures.put(seq, future);
						WebsocketUtil.sendMessage(mapper.writeValueAsString(cmd), agent.getIpAddress());
						String result = future.get(5, TimeUnit.SECONDS);
					} catch (JsonProcessingException | InterruptedException | ExecutionException | TimeoutException e) {
						;
					}

				}
			}
		});
		//update database
		return agentTaskRepository.cancelTask(instId);

	}

	/**
	 * 该方法用于真正分配任务列表
	 */
//	private void realAssignmentsTask(String ids) {
//		executor.execute(new AgentExecutor(ids));
//	}

	/**
	 * 该方法用于根据id获取对应任务记录
	 */
	
	public AgentTask findTaskById(Long id) {
		return agentTaskRepository.findAgentTaskById(id);
	}

	/**
	 * 该方法用于更新agent任务相关所属agentId
	 * @param taskId
	 * @param agentId
	 */
	
	public int updateAgentTaskAgentId(Long taskId, Long agentId) {
		Object[] params = new Object[2];
		params[0] = agentId;
		params[1] = taskId;
		return agentTaskRepository.updateAgentTaskAgentId(params);
	}

	
	public int batchUpdateAgentTaskAbandon(List<Object[]> params) {
		return agentTaskRepository.batchUpdateAgentTaskAbandon(params, needRetry);
	}

	
	public String insertAgentTask(List<Map<String, String>> collectors) {
		String result = ApiConstant.SUCCESS;
		List<Object[]> params = new ArrayList<>();
		collectors.forEach(c -> {
			String protocol = c.get(ApiConstant.PROTOCOL);
			String instanceId = c.get(ApiConstant.INSTANCEID);
			//首先根据协议及实例化id获取原始记录
			if (!needAddRecord(protocol, instanceId))
				return;
			Object[] param = new Object[4];
			param[0] = instanceId;
			param[1] = 0;
			param[2] = protocol;
			param[3] = DatetimeUtil.getCurrentTimestamp();
			params.add(param);
		});
		if (params.size() <= 0)
			return result;
		int resultInsert = agentTaskRepository.insertAgentTask(params);
		if (resultInsert <= 0)
			result = ApiConstant.FAIL;
		return result;
	}

	/**
	 * 该方法用于判定是否还需插入新纪录
	 * @return
	 */
	private boolean needAddRecord(String protocol, String instanceId) {
		Object[] param = new Object[2];
		param[0] = instanceId;
		param[1] = protocol;
		AgentTask task = agentTaskRepository.getAgentTaskByProtocolAndInstId(param);
		if (task == null)
			return true;
		int status = task.getStatus();
		Long agentId = task.getAgentId();
		if (agentId == null && status == 0)
			agentTaskRepository.updateAgentTaskPending(new Object[] { task.getId() });
		return false;
	}

	
	public int updateAgentTaskPending(String ip) {
		if (StringUtils.isEmpty(ip))
			return 0;
		String[] ips = ip.split(POINT);
		List<Object[]> params = new ArrayList<>(ips.length);
		StringBuilder ipStr = new StringBuilder();
		for (String p : ips) {
			ipStr.append(",'").append(p).append("'");
			Object[] param = new Object[2];
			param[0] = 0;
			param[1] = p;
			params.add(param);
		}
		agentRepository.updateAgentCurrentTaskByIp(params);//首先更新相关记录
		List<Long> resultIds = agentRepository.findAgentIdByIp(ipStr.substring(1));
		if (resultIds == null)
			return 0;
		List<Object[]> ids = new ArrayList<>();
		for (Long id : resultIds) {
			Object[] param = new Object[1];
			param[0] = id;
			ids.add(param);
		}
		return agentTaskRepository.batchUpdateAgentTaskPending(ids);
	}

	
	public void removeTasks(String instId) {
		cancelTask(instId);
		agentTaskRepository.removeInstanceTasks(instId);
	}

	
	public void deleteTaskById(Long id) {
		agentTaskRepository.deleteTaskById(id);
	}

	
	public void insertLinkTask(String instId) {
		//首先根据instId查找是否有记录，如果有，直接更新记录状态
		boolean needAddRecord = needAddRecord("snmp", instId);
		if (needAddRecord) {
			List<Object[]> params = new ArrayList<>();
			Object[] param = new Object[4];
			param[0] = instId;
			param[1] = 0;
			param[2] = "snmp";
			param[3] = DatetimeUtil.getCurrentTimestamp();
			params.add(param);
			agentTaskRepository.insertAgentTask(params);
		}
	}

	
	public void updateCollectCmd() {
		agentTaskRepository.updateCollectCmd();
	}

}
