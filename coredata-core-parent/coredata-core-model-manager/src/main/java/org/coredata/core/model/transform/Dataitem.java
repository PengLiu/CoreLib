package org.coredata.core.model.transform;

import java.io.Serializable;

public class Dataitem implements Serializable {

	private static final long serialVersionUID = 6118559675321543169L;

	private String key;

	private String name;

	private String datatype;

	private String unit;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

}
