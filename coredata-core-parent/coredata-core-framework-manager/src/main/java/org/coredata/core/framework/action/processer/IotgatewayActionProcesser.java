package org.coredata.core.framework.action.processer;

import com.fasterxml.jackson.databind.JsonNode;
import org.coredata.core.framework.util.CRC16;
import org.coredata.core.model.action.model.Controller;
import org.coredata.core.model.action.model.SendController;
import org.coredata.core.model.collection.Param;
import org.coredata.core.model.constants.ClientConstant;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用于物联网关动作指令的处理
 * @author ChengYongfei
 *
 */
@Service
public class IotgatewayActionProcesser extends ActionProcesser {

	private static final String paramExp = "\\$\\{(.*?)\\}";
	private static final Pattern paramPattern = Pattern.compile(paramExp);

	@Override
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
		sendController.setInstanceId(instance.get("uniqueIdent").asText());
		sendController.setCmd(getCmdString(controller.getCmd(), instance, controller));
		sendControllers.add(sendController);
		params.put(ClientConstant.CONTROLLER, sendControllers);
	}

	@Override
	protected String getCmdString(String cmd, JsonNode instance, Controller controller) {
		String result = cmd;
		List<Param> param = controller.getParam();
		Map<String, String> paramMap = new HashMap<String, String>();
		param.forEach(p -> {
			paramMap.put(p.getKey(), p.getValue());
		});
		Matcher dsMatcher = paramPattern.matcher(result);
		int index = Integer.parseInt(instance.get("index").asText());
		int len = Integer.valueOf(paramMap.get("indexLength") == null ? "1" : paramMap.get("indexLength"));
		while (dsMatcher.find()) {
			String paramStr = dsMatcher.group(1);
			switch (paramStr) {
			case "index":
				int realIndex = 0;
				if (len == 1) {
					realIndex = index - 1;
				} else {
					realIndex = index / len;
				}
				String indexStr = Integer.toHexString(realIndex);
				indexStr = indexStr.length() == 1 ? indexStr = "0" + indexStr : indexStr;
				result = result.replace(dsMatcher.group(), indexStr);
				break;
			case "status":
				int statusIndex = 1;
				if (len == 1) {
					statusIndex = 1;
				} else {
					statusIndex = index % len;
				}
				String statusStr = "";
				for (int i = 1; i <= len; i++) {
					if (i == statusIndex) {
						statusStr = statusStr + paramMap.get("status");
					} else {
						statusStr = statusStr + paramMap.get("other");
					}
				}
				result = result.replace(dsMatcher.group(), statusStr);
				break;
			case "CRC":
				result = CRC16.getBufHexStr(CRC16.getSendBuf(result.replace(" ", "").replace(dsMatcher.group(), "")));
				break;
			default:
				break;
			}

		}
		return result;
	}

}
