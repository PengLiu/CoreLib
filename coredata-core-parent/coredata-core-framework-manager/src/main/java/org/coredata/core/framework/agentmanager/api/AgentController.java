package org.coredata.core.framework.agentmanager.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.coredata.core.framework.agentmanager.dto.ActionDto;
import org.coredata.core.framework.agentmanager.dto.AgentDto;
import org.coredata.core.framework.agentmanager.entity.Agent;
import org.coredata.core.framework.agentmanager.monitor.AgentMonitor;
import org.coredata.core.framework.agentmanager.page.PageParam;
import org.coredata.core.framework.agentmanager.service.AgentManagerService;
import org.coredata.core.framework.agentmanager.service.AgentTaskService;
import org.coredata.core.framework.agentmanager.util.LogUtil;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;
import org.coredata.core.framework.agentmanager.websocket.WebsocketUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AgentController extends BaseController {

	@Autowired
	private AgentManagerService agentService;

	@Autowired
	private AgentTaskService agentTaskService;

	/**
	 * 取得客户所有Agent信息
	 * @param customerId
	 * @return
	 */
	@RequestMapping(path = "/agents", method = RequestMethod.GET)
	public String getAgents(@RequestHeader(value = CUSTOMER_ID, required = false) String customerId) {
		List<Agent> agent = agentService.findAgentsByCustomerId(customerId);
		return JSON.toJSONString(agent);
	}

	/**
	 * 取得客户所有Agent任务总数
	 * @param response
	 * @return
	 */
	@RequestMapping("/agent/tasks")
	public int tasksCount(@RequestHeader(value = CUSTOMER_ID, required = false) String customerId) {
		return AgentMonitor.getTaskCount();
	}

	/**
	 * 根据实例id删除任务
	 * @param instId
	 * @param response
	 */
	@RequestMapping("/agent/removeTasks/{instId}")
	public void removeTasks(@PathVariable String instId, HttpServletResponse response) {
		try {
			agentTaskService.removeTasks(instId);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Cancel " + instId + " task error", e);
		}
	}

	//	@RequestMapping("/agent/cancelMonitor/{instId}")
	//	public int cancelMonitor(@PathVariable String instId, HttpServletResponse response) {
	//		try {
	//			return agentTaskService.cancelTask(instId);
	//		} catch (Exception e) {
	//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	//			logger.error("Cancel " + instId + " task error", e);
	//			return 0;
	//		}
	//	}

	//	@RequestMapping("/agent/addMonitor/{instId}")
	//	public int addMonitor(@PathVariable String instId, HttpServletResponse response) {
	//		try {
	//			return agentTaskService.addMonitor(instId);
	//		} catch (Exception e) {
	//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	//			logger.error("Cancel " + instId + " task error", e);
	//			return -1;
	//		}
	//	}

	/**
	 * 该方法用于返回前台agent管理列表
	 * @param agentDto
	 * @return
	 */
	@RequestMapping("/agent")
	public String findAllAgent(@ModelAttribute("params") AgentDto agentDto) {
		PageParam<Agent> agents = agentService.findPagingAgent(agentDto);
		return JSON.toJSONString(agents);
	}

	/**
	 * 该方法用于根据agentId返回相关setting内容
	 * @param agentId
	 * @return
	 */
	@RequestMapping("/agent/{agentId}")
	public String getAgentSetting(@PathVariable Long agentId) {
		Map<String, Object> settings = agentService.findAgentSettingByAgentId(agentId);
		return JSON.toJSONString(settings);
	}

	@RequestMapping("/send/{msg}")
	public void sendMessage(@PathVariable String msg) {
		WebsocketUtil.sendMessage(null, msg);
	}

	//	/**
	//	 * 该方法用于告诉Agent正式采集相关数据
	//	 * @return
	//	 */
	//	@RequestMapping(value = "/agents/collect", method = RequestMethod.POST)
	//	public String agentCollect(@RequestBody String cmd) {
	//		String result = ApiConstant.SUCCESS;
	//		try {
	//			result = agentService.sendAgentCmd(cmd);
	//		} catch (Exception e) {
	//			logger.error(LogUtil.stackTraceToString(e));
	//			result = processExceptionResponse(cmd);
	//		}
	//		return result;
	//	}

	/**
	 * 该方法用于协议测试，请求agent
	 * @return
	 */
	@RequestMapping(value = "/agents/test", method = RequestMethod.POST)
	public String agentProtocolTest(@RequestBody String cmd) {
		String result = ApiConstant.SUCCESS;
		try {
			result = agentService.sendAgentCmd(cmd);
		} catch (Exception e) {
			logger.error(LogUtil.stackTraceToString(e));
			result = processExceptionResponse(cmd);
		}
		return result;
	}

	/**
	 * 该方法用于协议测试，请求agent
	 * @return
	 */
	@RequestMapping(value = "/agents/instance", method = RequestMethod.POST)
	public String agentInstance(@RequestBody String cmd) {
		String result = ApiConstant.SUCCESS;
		try {
			result = agentService.sendAgentCmd(cmd);
		} catch (Exception e) {
			logger.error(LogUtil.stackTraceToString(e));
			result = processExceptionResponse(cmd);
		}
		return result;
	}

	/**
	 * 该方法用于保存对应采集任务
	 * @return
	 */
	@RequestMapping(value = "/agent/tasks", method = RequestMethod.POST)
	public String insertAgentTask(@RequestBody List<Map<String, String>> collectors) {
		String result = ApiConstant.SUCCESS;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Start insert agent task " + collectors.size());
			}
			result = agentTaskService.insertAgentTask(collectors);
			if (logger.isDebugEnabled()) {
				logger.debug("End insert agent task " + result);
			}
		} catch (Exception e) {
			logger.error(LogUtil.stackTraceToString(e));
			result = ApiConstant.FAIL;
		}
		return result;
	}

	/**
	 * 该方法用于处理异常响应
	 * @return
	 */
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

	/**
	 * 该方法用于告诉Agent执行相关动作指令
	 * @return
	 */
	@RequestMapping(value = "/agents/action", method = RequestMethod.POST)
	public String agentAction(@RequestBody String cmd) {
		String result = ApiConstant.SUCCESS;
		try {
			result = agentService.sendAgentCmd(cmd);
		} catch (Exception e) {
			logger.error(LogUtil.stackTraceToString(e));
			result = processExceptionResponse(cmd);
		}
		return result;
	}

	/**
	 * 用于向采集任务表中插入链路采集任务
	 * @param instId
	 */
	@RequestMapping(value = "/links/task/{instId}", method = RequestMethod.GET)
	public void insertLinkMonitor(@PathVariable String instId) {
		agentTaskService.insertLinkTask(instId);
	}

	/**
	 * 用于更改某个资产的状态
	 * @param dto
	 * @return
	 */
	@RequestMapping(value = "/res/actions/status", method = RequestMethod.POST)
	public String changeInstanceStatus(@RequestBody ActionDto dto) {
		String result = ApiConstant.SUCCESS;
		try {
			result = agentService.changeInstanceStatus(dto);
		} catch (Exception e) {
			logger.error("changeInstanceStatus (" + JSON.toJSONString(dto) + "), result (" + result + ")", e);
		}
		return result;
	}

	/**
	 * 用于开启/关闭某个服务，例如tcp，snmptrap等
	 * @return
	 */
	@RequestMapping(value = "/servers", method = RequestMethod.POST)
	public String requestServer(@RequestBody String params) {
		return agentService.processServerRequest(params);
	}

}
