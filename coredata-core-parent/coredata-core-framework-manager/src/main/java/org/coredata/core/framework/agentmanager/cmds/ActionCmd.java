package org.coredata.core.framework.agentmanager.cmds;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.cmds.response.ActionResponse;
import org.coredata.core.framework.agentmanager.dto.ControllerDTO;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;
import org.coredata.core.model.constants.ApiConstant;

import java.util.ArrayList;
import java.util.List;


public class ActionCmd extends Command {
	private static final Logger logger = Logger.getLogger(ActionCmd.class);

	private String id;
	
	private String instanceId;
	
	private String modelId;

	/**
	 * 此次发给Agent请求动作
	 */
	private String action = WebsocketConstant.ACTION_ACTION;

	/**
	 * 接取动作模型相关case
	 */
	private List<ControllerDTO> controller = new ArrayList<ControllerDTO>();

	/**
	 * 动作命令对应响应类
	 */
	private ActionResponse response;

	@Override
	public void processResult(String result) {
		//处理结果
		if (logger.isDebugEnabled())
			logger.debug("Recive Message ::: " + result);
		if (ApiConstant.TIME_OUT.equals(result))
			this.response = new ActionResponse(this, result);
		else
			this.response = JSON.parseObject(result, ActionResponse.class);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}



	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the response
	 */
	public ActionResponse getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(ActionResponse response) {
		this.response = response;
	}

	/**
	 * @return the controller
	 */
	public List<ControllerDTO> getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(List<ControllerDTO> controller) {
		this.controller = controller;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the modelId
	 */
	public String getModelId() {
		return modelId;
	}

	/**
	 * @param modelId the modelId to set
	 */
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}





}
