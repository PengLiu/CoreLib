package org.coredata.core.util.querydsl.exception;

public class QuerydslException extends Exception {

	private static final long serialVersionUID = 8945108398512168435L;

	private Exception e;

	private String msg;

	public QuerydslException(String msg, Exception e) {
		this.msg = msg;
		this.e = e;
	}

	public QuerydslException(String msg) {
		this(msg, null);
	}

	public String getMsg() {
		return msg;
	}

	public Exception getE() {
		return e;
	}

}
