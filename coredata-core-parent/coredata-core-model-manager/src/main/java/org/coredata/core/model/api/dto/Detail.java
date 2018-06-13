package org.coredata.core.model.api.dto;

import java.util.List;

public class Detail {

	private String id;

	private String value;

	/**
	 * 属性显示名称
	 */
	private String displayName;

	/**
	 * 是否可以编辑属性
	 */
	private String isEditable;

	/**
	 * 是否默认选中属性
	 */
	private boolean defaultSelected = false;
	
	/**
	 * 相关联的属性集合
	 */
	private List<InstanceProperty> props;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getIsEditable() {
		return isEditable;
	}

	public void setIsEditable(String isEditable) {
		this.isEditable = isEditable;
	}

	public boolean isDefaultSelected() {
		return defaultSelected;
	}

	public void setDefaultSelected(boolean defaultSelected) {
		this.defaultSelected = defaultSelected;
	}

	/**
	 * @return the props
	 */
	public List<InstanceProperty> getProps() {
		return props;
	}

	/**
	 * @param props the props to set
	 */
	public void setProps(List<InstanceProperty> props) {
		this.props = props;
	}

}
