package org.coredata.core.framework.agentmanager.cmds;


import org.coredata.core.framework.agentmanager.websocket.WebsocketConstant;

public class CollectNowCmd extends CollectCmd {

	private String action = WebsocketConstant.ACTION_REALTIME_COLLECT;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
