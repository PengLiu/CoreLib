package org.coredata.core.model.discovery;

import java.io.Serializable;

public class InstanceIndex implements Serializable {

	private static final long serialVersionUID = 421459171636629866L;

	private String method;

	private String format;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
