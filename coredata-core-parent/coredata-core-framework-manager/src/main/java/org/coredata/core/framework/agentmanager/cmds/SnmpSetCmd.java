package org.coredata.core.framework.agentmanager.cmds;


import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

public class SnmpSetCmd extends Command {

	/**
	 * 此次发给Agent请求动作
	 */
	private String action = WebsocketConstant.ACTION_SNMP_SET;

	/**
	 * 发送给snmpset的OID
	 */
	private String oid;

	/**
	 * 发送给snmp设备的状态
	 */
	private Integer state;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

}
