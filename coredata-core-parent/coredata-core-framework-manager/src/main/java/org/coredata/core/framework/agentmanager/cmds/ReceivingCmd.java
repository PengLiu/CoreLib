package org.coredata.core.framework.agentmanager.cmds;


import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReceivingCmd extends Command {

	/**
	 * 对应协议
	 */
	private String protocol;

	/**
	 * 采集命令id
	 */
	private String id;

	/**
	 * 此次发给Agent请求动作
	 */
	protected String action = WebsocketConstant.ACTION_RECEIVING;

	/**
	 * 接取采集模型相关case
	 */
	private List<Map<String, Object>> collector = new ArrayList<>();

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

}
