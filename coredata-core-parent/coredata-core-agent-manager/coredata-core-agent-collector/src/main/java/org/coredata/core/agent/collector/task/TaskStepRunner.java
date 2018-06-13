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
import org.coredata.core.agent.collector.result.CmdStepDataResult;
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
public class TaskStepRunner extends UntypedAbstractActor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private Collector collector;

	@Override
	@SuppressWarnings("unchecked")
	public void onReceive(Object obj) throws Throwable {

		if (obj instanceof Map) {
			Map<Long, List<Cmd>> cmds = (Map<Long, List<Cmd>>) obj;
			for (Entry<Long, List<Cmd>> entry : cmds.entrySet()) {
				for (Cmd cmd : entry.getValue()) {
					collector.run(cmd, getSelf());
				}
			}
		} else if (obj instanceof CmdResult) {

			CmdResult r = (CmdResult) obj;
			Cmd cmd = r.getCmd();

			if (logger.isDebugEnabled()) {
				logger.debug("TaskStepRunner Cmd result :" + r.toString());
			}

			CmdStepDataResult result = new CmdStepDataResult();
			result.setSeq(cmd.getSeq());
			result.setCustomerId(AgentSettings.getCustomerId());
			result.setStep(cmd.getStep());
			result.setName(cmd.getName());
			result.setModelId(cmd.getModelId());
			result.setParams(cmd.getParams());
			result.setType(TaskType.RunStep.toString());

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
				result.setTasktime(cmd.getTasktimes().get(0));
				sendResult(result);
			}

		} else {
			unhandled(obj);
		}

	}

	void sendResult(CmdStepDataResult result) {
		try {
			if (AgentConfig.packageFlag) {
				Map<String, String> returnMap = new HashMap<>();
				returnMap.put("AgentDataType", "cmdStepDataResult");
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