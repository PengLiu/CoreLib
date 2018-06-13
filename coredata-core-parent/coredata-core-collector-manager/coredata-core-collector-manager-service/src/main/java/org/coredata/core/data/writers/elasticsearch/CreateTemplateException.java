package org.coredata.core.data.writers.elasticsearch;

public class CreateTemplateException extends Exception {

	private static final long serialVersionUID = -8513897340975165793L;

	private String msg;

	public CreateTemplateException(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

}
