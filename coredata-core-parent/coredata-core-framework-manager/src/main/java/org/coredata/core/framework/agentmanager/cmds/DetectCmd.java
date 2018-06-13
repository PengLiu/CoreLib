package org.coredata.core.framework.agentmanager.cmds;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.cmds.response.DetectResponse;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

import java.util.List;
import java.util.Map;

public class DetectCmd extends Command {

	private static final Logger logger = Logger.getLogger(DetectCmd.class);

	/**
	 * 此次发给Agent请求动作
	 */
	private String action = WebsocketConstant.ACTION_DETECT;

	/**
	 * 对应响应类型
	 */
	private DetectResponse response;

	@Override
	public void processResult(String result) {
		//处理结果
		if (logger.isDebugEnabled())
			logger.debug("Recive Message ::: " + result);
		this.response = JSON.parseObject(result, DetectResponse.class);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public DetectResponse getResponse() {
		return response;
	}

	public void setResponse(DetectResponse response) {
		this.response = response;
	}

	/**
	 * 该方法用于拼接检测命令
	 * @return
	 */
	public String joinDetectCmd(List<Map<String, String>> connections) {
		this.setConnections(connections);
		return JSON.toJSONString(this);
	}

}
