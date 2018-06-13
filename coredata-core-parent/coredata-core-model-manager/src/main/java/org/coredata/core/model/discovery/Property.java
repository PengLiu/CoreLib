package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Property implements Serializable {

	private static final long serialVersionUID = -8815078765339303261L;

	private String id;

	private String name;

	private PropertyValue value;

	/**
	 * 是否可以被编辑选项
	 */
	private String maintenancetype;

	/**
	 * 是否在前台显示功能
	 */
	private String isdisplay;

	/**
	 * 表明该属性类型，common为公有属性
	 */
	private String fieldtype;

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

	public String getMaintenancetype() {
		return maintenancetype;
	}

	public void setMaintenancetype(String maintenancetype) {
		this.maintenancetype = maintenancetype;
	}

	public String getIsdisplay() {
		return isdisplay;
	}

	public void setIsdisplay(String isdisplay) {
		this.isdisplay = isdisplay;
	}

	public PropertyValue getValue() {
		return value;
	}

	public void setValue(PropertyValue value) {
		this.value = value;
	}

	public String getFieldtype() {
		return fieldtype;
	}

	public void setFieldtype(String fieldtype) {
		this.fieldtype = fieldtype;
	}

}
