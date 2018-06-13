package org.coredata.core.model.discovery;

import java.io.Serializable;

public class PropertyValue implements Serializable {

	private static final long serialVersionUID = -7851680485670111854L;

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
