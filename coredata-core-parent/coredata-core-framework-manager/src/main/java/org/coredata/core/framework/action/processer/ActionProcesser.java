package org.coredata.core.framework.action.processer;

import com.fasterxml.jackson.databind.JsonNode;
import org.coredata.core.model.action.model.Controller;
import org.coredata.core.model.action.model.SendController;
import org.coredata.core.model.constants.ClientConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 
 * @author ChengYongfei
 *
 */
public abstract class ActionProcesser {

	public void processActionParams(Map<String, Object> params, JsonNode instance, Controller controller, Map<String, String> connectInfo) {
		params.put(ClientConstant.SERVER_REQUEST_ACTION, ClientConstant.SERVER_REQUEST_ACTION_ACTION);
		params.put(ClientConstant.SERVER_REQUEST_SEQ, UUID.randomUUID().toString());
		List<Map<String, String>> connections = new ArrayList<Map<String, String>>();
		connections.add(connectInfo);
		params.put(ClientConstant.SERVER_REQUEST_CONNECT, connections);
		List<SendController> sendControllers = new ArrayList<SendController>();

		String modelId = instance.get("modelId").asText();
		SendController sendController = new SendController();
		sendController.setId(controller.getId());
		sendController.setName(controller.getName());
		sendController.setProtocol(connectInfo.get(ClientConstant.PROTOCOL));
		sendController.setModelId(modelId);
		sendController.setCollectType("action");
		sendController.setInstanceId(instance.get("uniqueIdent").asText());
		sendController.setCmd(getCmdString(controller.getCmd(), instance, controller));
		sendControllers.add(sendController);
		params.put(ClientConstant.CONTROLLER, sendControllers);
	}

	protected abstract String getCmdString(String cmd, JsonNode instance, Controller controller);

}
