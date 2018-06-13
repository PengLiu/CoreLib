package org.coredata.core.framework.agentmanager.cmds;

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.cmds.response.TestResponse;
import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCmd extends Command {

	private static final Logger logger = Logger.getLogger(TestCmd.class);

	/**
	 * 此次发给Agent请求动作
	 */
	private String action = WebsocketConstant.ACTION_TEST;

	/**
	 * 接取测试相关case
	 */
	private List<Map<String, String>> prerequired = new ArrayList<>();

	/**
	 * 对应响应类型
	 */
	private TestResponse response;

	public TestCmd() {

	}

	public TestCmd(String result) {
		super.setResult(result);
	}

	@Override
	public void processResult(String result) {
		//处理结果
		if (logger.isDebugEnabled())
			logger.debug("Recive Message ::: " + result);
		this.response = JSON.parseObject(result, TestResponse.class);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public TestResponse getResponse() {
		return response;
	}

	public void setResponse(TestResponse response) {
		this.response = response;
	}

	public List<Map<String, String>> getPrerequired() {
		return prerequired;
	}

	public void setPrerequired(List<Map<String, String>> prerequired) {
		this.prerequired = prerequired;
	}

	@Override
	public String getResult() {
		return super.getResult();
	}

}
