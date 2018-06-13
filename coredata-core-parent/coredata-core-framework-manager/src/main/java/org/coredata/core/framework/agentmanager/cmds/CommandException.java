package org.coredata.core.framework.agentmanager.cmds;

public class CommandException extends Exception {

	private static final long serialVersionUID = -210882713830746679L;

	private String msg;

	private Exception e;

	public CommandException(String msg, Exception e) {
		this.msg = msg;
		this.e = e;
	}

	public CommandException(String msg) {
		this.msg = msg;
	}

	public CommandException(Exception e) {
		this.e = e;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Exception getE() {
		return e;
	}

	public void setE(Exception e) {
		this.e = e;
	}

}
