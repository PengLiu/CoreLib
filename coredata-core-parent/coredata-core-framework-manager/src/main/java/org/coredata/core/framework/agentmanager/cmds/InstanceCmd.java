package org.coredata.core.framework.agentmanager.cmds;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.cmds.response.InstanceResponse;
import org.coredata.core.framework.agentmanager.dto.InstanceDto;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstanceCmd extends Command {

	private static final Logger logger = Logger.getLogger(InstanceCmd.class);

	/**
	 * 此次发给Agent请求动作
	 */
	private String action = WebsocketConstant.ACTION_INSTANCE;

	/**
	 * 接取实例化相关case
	 */
	private List<InstanceDto> instance = new ArrayList<>();

	/**
	 * 实例化相关case中可能使用到的变量列表
	 */
	private List<Map<String, String>> params;

	/**
	 * 对应响应类型
	 */
	private InstanceResponse response;

	public InstanceCmd() {

	}

	public InstanceCmd(String result) {
		super.setResult(result);
	}

	@Override
	public void processResult(String result) {
		//处理结果
		if (logger.isDebugEnabled())
			logger.debug("Recive Message ::: " + result);
		this.response = JSON.parseObject(result, InstanceResponse.class);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<InstanceDto> getInstance() {
		return instance;
	}

	public void setInstance(List<InstanceDto> instance) {
		this.instance = instance;
	}

	@Override
	public String getResult() {
		return super.getResult();
	}

	public InstanceResponse getResponse() {
		return response;
	}

	public void setResponse(InstanceResponse response) {
		this.response = response;
	}

	public List<Map<String, String>> getParams() {
		return params;
	}

	public void setParams(List<Map<String, String>> params) {
		this.params = params;
	}

}
