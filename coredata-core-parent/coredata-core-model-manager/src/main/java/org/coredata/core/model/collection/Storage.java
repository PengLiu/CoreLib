package org.coredata.core.model.collection;

import java.io.Serializable;

public class Storage implements Serializable {

	private static final long serialVersionUID = 7197707341916133945L;

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
