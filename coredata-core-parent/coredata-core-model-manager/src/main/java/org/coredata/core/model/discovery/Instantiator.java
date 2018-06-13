package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Instantiator implements Serializable {

	private static final long serialVersionUID = 4305369471418932930L;

	private String method;

	private String[] params;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String[] getParams() {
		return params;
	}

	public void setParams(String[] params) {
		this.params = params;
	}

}
