package org.coredata.core.model.api.dto;

import java.io.Serializable;

public class InstanceProperty implements Serializable {

	private static final long serialVersionUID = 7005025288312633122L;

	/**
	 * 保存实例id。对应的是实例中uniqueIdent唯一标识
	 */
	private String instId;

	/**
	 * 保存属性id值
	 */
	private String propertyId;

	/**
	 * 保存属性name值
	 */
	private String propertyName;

	/**
	 * 保存属性值
	 */
	private String propertyValue;

	/**
	 * 保存该属性是否可编辑状态
	 */
	private String maintenanceType;

	/**
	 * 保存该属性是否在页面显示
	 */
	private String isDisplay;

	public String getInstId() {
		return instId;
	}

	public void setInstId(String instId) {
		this.instId = instId;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public String getMaintenanceType() {
		return maintenanceType;
	}

	public void setMaintenanceType(String maintenanceType) {
		this.maintenanceType = maintenanceType;
	}

	public String getIsDisplay() {
		return isDisplay;
	}

	public void setIsDisplay(String isDisplay) {
		this.isDisplay = isDisplay;
	}

}
