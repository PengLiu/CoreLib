package org.coredata.core.datastream.blueprint.exception;

public class BluePrintException extends Exception {

	private static final long serialVersionUID = -8295123516874379906L;

	private String msg;

	private Throwable exception;

	public BluePrintException(String msg, Throwable e) {
		this.msg = msg;
		this.exception = e;
	}

	public BluePrintException(String msg) {
		this(msg, null);
	}

	public String getMsg() {
		return msg;
	}

	public Throwable getException() {
		return exception;
	}

}
