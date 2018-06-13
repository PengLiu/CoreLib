package org.coredata.core.agent.collector.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.agent.collector.Cmd;
import org.coredata.core.agent.collector.config.AgentConfig;
import org.coredata.core.agent.collector.config.AgentSettings;
import org.coredata.core.agent.collector.result.CmdDataResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.UntypedAbstractActor;

@Component
@Scope("prototype")
public class TaskReceivingRunner extends UntypedAbstractActor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	final ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object obj) throws Throwable {
		if (obj instanceof List) {
			List<Cmd> cmds = (List<Cmd>) obj;
			if (!CollectionUtils.isEmpty(cmds)) {
				for (Cmd cmd : cmds) {
					CmdDataResult result = new CmdDataResult();
					result.setName(cmd.getName());
					result.setType(TaskType.RunReceiving.toString());
					result.setInstanceId(cmd.getInstanceId());
					result.setModelId(cmd.getModelId());
					result.setParams(cmd.getParams());
					result.setCustomerId(AgentSettings.getCustomerId());
					result.setSuccess(true);
					result.setMsg(cmd.getResult());
					result.setTasktime(System.currentTimeMillis());
					sendResult(result);
				}
			}
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
