package org.coredata.core.agent.collector.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coredata.core.agent.collector.Cmd;
import org.coredata.core.agent.collector.CmdResult;
import org.coredata.core.agent.collector.Collector;
import org.coredata.core.agent.collector.config.AgentConfig;
import org.coredata.core.agent.collector.config.AgentSettings;
import org.coredata.core.agent.collector.result.CmdDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.UntypedAbstractActor;

@Component
@Scope("prototype")
public class TaskPeriodRunner extends UntypedAbstractActor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	final ObjectMapper mapper = new ObjectMapper();

	Task task = null;

	final String MODEL_INS_ID = "modelId";

	final String INSTANCERESTYPE_UNIQ = "uniqueIdent";

	final String INSTANCERE_INDEX = "index";

	@Autowired
	private Collector collector;

	public TaskPeriodRunner(Task task) {
		this.task = task;
		if (logger.isDebugEnabled()) {
			for (Entry<Long, List<Cmd>> entry : task.getCmds().entrySet()) {
				for (Cmd cmd : entry.getValue()) {
					logger.debug("Create task for instance " + cmd.getInstanceId() + " and cmd " + cmd.getCmd());
				}
			}
		}
	}

	@Override
	public void onReceive(Object obj) throws Throwable {
		if (obj instanceof Long) {
			long current = (Long) obj;
			for (Entry<Long, List<Cmd>> entry : task.getCmds().entrySet()) {
				for (Cmd cmd : entry.getValue()) {
					if ((current / 1000 - cmd.getFirstTimestamp()) % entry.getKey() == 0) {
						collector.run(cmd, getSelf());
					}
				}
			}
		} else if (obj instanceof CmdResult) {

			CmdResult r = (CmdResult) obj;
			Cmd cmd = r.getCmd();

			if (logger.isDebugEnabled()) {
				logger.debug("TaskPeriodRunner Cmd result :" + r.toString());
			}

			CmdDataResult result = new CmdDataResult();
			result.setName(cmd.getName());
			result.setInstanceId(cmd.getInstanceId());
			result.setNodeId(task.getTaskId());
			result.setModelId(cmd.getModelId());
			result.setParams(cmd.getParams());
			result.setCustomerId(AgentSettings.getCustomerId());

			if (r.getErr() != null) {
				result.setSuccess(false);
				if (CollectionUtils.isEmpty(cmd.getTasktimes())) {
					result.setTasktime(System.currentTimeMillis());
				} else {
					result.setTasktime(cmd.getTasktimes().get(0));
				}
				result.setErr(r.getErr().toString());
				sendResult(result);
				return;
			} else {
				result.setSuccess(true);
				result.setMsg(r.getVal());

				r.getTaskTimes().forEach(taskTime -> {
					result.setTasktime(taskTime);
					if (cmd.getIsGlobalResult()) {
						for (Map<String, String> info : cmd.getSubInstanceInfo()) {
							CmdDataResult subresult = new CmdDataResult();
							subresult.setName(cmd.getName());
							subresult.setInstanceId(info.get(INSTANCERESTYPE_UNIQ));
							subresult.setNodeId(task.getTaskId());
							subresult.setModelId(info.get(MODEL_INS_ID));
							subresult.setParams(cmd.getParams());
							subresult.setSuccess(true);
							subresult.setMsg(r.getVal());
							subresult.setIndex(info.get(INSTANCERE_INDEX));
							subresult.setTasktime(taskTime);
							sendResult(subresult);
						}
					} else {
						sendResult(result);
					}

				});
			}

		} else {
			unhandled(obj);
		}
	}

	void sendResult(CmdDataResult result) {

		try {
			if (AgentConfig.packageFlag) {
				Map<String, String> returnMap = new HashMap<>();
				returnMap.put("AgentDataType", "cmdDataResult");
				String val = mapper.writeValueAsString(result);
				returnMap.put("data", val);
				//TODO 转发采集结果
				//				AgentDataProcesser.setData(JSON.toJSONString(returnMap));
			} else {
				String val = mapper.writeValueAsString(result);
				if (logger.isDebugEnabled())
					logger.debug("Collect result is : " + val);
				//TODO 转发采集结果
				//				output.output(val);
			}
		} catch (JsonProcessingException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Parse result as json error ", e);
			}
		}

	}

}