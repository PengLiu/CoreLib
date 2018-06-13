package org.coredata.core.model.discovery;

import java.io.Serializable;

public class InstanceName implements Serializable {

	private static final long serialVersionUID = -1847693807755569649L;

	private String format;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
