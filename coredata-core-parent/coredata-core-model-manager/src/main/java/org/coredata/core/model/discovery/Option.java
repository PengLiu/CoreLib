package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Option implements Serializable {

	private static final long serialVersionUID = 2647731977088016760L;

	private String value;

	private String title;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
