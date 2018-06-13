package org.coredata.core.model.common;

import java.io.Serializable;

public class Vendor implements Serializable {

	private static final long serialVersionUID = 6915741407551504805L;

	private String id;

	private String name;

	private String vendoricon;

	private String[] restypes;

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

	public String getVendoricon() {
		return vendoricon;
	}

	public void setVendoricon(String vendoricon) {
		this.vendoricon = vendoricon;
	}

	public String[] getRestypes() {
		return restypes;
	}

	public void setRestypes(String[] restypes) {
		this.restypes = restypes;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
