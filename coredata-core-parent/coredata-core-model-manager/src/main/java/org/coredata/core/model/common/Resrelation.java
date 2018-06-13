package org.coredata.core.model.common;

import java.io.Serializable;

public class Resrelation implements Serializable {

	private static final long serialVersionUID = 9222175025945539974L;

	private String id;

	private String name;

	/**
	 * 是否系统默认模型，默认是
	 */
	private int isSystem = 1;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
