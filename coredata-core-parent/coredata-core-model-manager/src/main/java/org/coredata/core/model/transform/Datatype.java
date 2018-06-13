package org.coredata.core.model.transform;

import java.io.Serializable;

public class Datatype implements Serializable {

	private static final long serialVersionUID = -8855260402210545313L;

	private String name;

	private String withheader;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWithheader() {
		return withheader;
	}

	public void setWithheader(String withheader) {
		this.withheader = withheader;
	}

}
