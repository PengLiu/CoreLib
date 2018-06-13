package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Validate implements Serializable {

	private static final long serialVersionUID = -630753877082125974L;

	private String method;

	private String errmsg;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

}
