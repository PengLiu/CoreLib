package org.coredata.core.framework.agentmanager.cmds;


import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

public class ServerCmd extends Command {

	/**
	 * 对应协议
	 */
	private String protocol;

	/**
	 * 此次发给Agent请求动作
	 */
	protected String action = WebsocketConstant.ACTION_SERVER;

	/**
	 * 此次服务开启的端口
	 */
	private int port;

	/**
	 * 此次开启服务名称
	 */
	private String serverName;

	/**
	 * 命令状态，1开启，0关闭
	 */
	private int serverStatus;

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(int serverStatus) {
		this.serverStatus = serverStatus;
	}
}
