package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Param implements Serializable {

    private static final long serialVersionUID = 1670059780016855634L;

	private String key;

	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
