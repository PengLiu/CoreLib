package org.coredata.core.framework.agentmanager.cmds;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.cmds.response.CollectResponse;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;
import org.coredata.core.model.constants.ApiConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectCmd extends Command {

	private static final Logger logger = Logger.getLogger(CollectCmd.class);

	private String id;

	/**
	 * 此次发给Agent请求动作
	 */
	protected String action = WebsocketConstant.ACTION_COLLECT;

	/**
	 * 接取采集模型相关case
	 */
	private List<Map<String, Object>> collector = new ArrayList<>();

	/**
	 * 采集命令对应响应类
	 */
	private CollectResponse response;

	@Override
	public void processResult(String result) {
		//处理结果
		if (logger.isDebugEnabled())
			logger.debug("Recive Message ::: " + result);
		if (ApiConstant.TIME_OUT.equals(result))
			this.response = new CollectResponse(this, result);
		else
			this.response = JSON.parseObject(result, CollectResponse.class);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<Map<String, Object>> getCollector() {
		return collector;
	}

	public void setCollector(List<Map<String, Object>> collector) {
		this.collector = collector;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CollectResponse getResponse() {
		return response;
	}

	public void setResponse(CollectResponse response) {
		this.response = response;
	}

}
