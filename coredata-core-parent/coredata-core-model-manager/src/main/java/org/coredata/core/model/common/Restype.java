package org.coredata.core.model.common;

import java.io.Serializable;

public class Restype implements Serializable {

	private static final long serialVersionUID = -8200911339410078953L;

	private String id;

	private String name;

	private String desc;

	private String parentid;

	private String level;

	private String isroot;

	private String obashilevel;

	private String[] attachrestypes;

	private String[] kpimetric;

	private String entitytype;

	private String fullPath;

	private boolean onlyclassify = true;

	/**
	 * 资源分类类型，是否为默认分类
	 */
	private Integer defaultType = 1;

	/**
	 * 是否有厂商标识
	 */
	private boolean hasFirm = false;

	/**
	 * 是否资产
	 */
	private boolean isAsset = false;

	/**
	 * 是否系统默认模型，默认是
	 */
	private int isSystem = 1;

	private String customerId = "";

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getIsroot() {
		return isroot;
	}

	public void setIsroot(String isroot) {
		this.isroot = isroot;
	}

	public String getObashilevel() {
		return obashilevel;
	}

	public void setObashilevel(String obashilevel) {
		this.obashilevel = obashilevel;
	}

	public String[] getAttachrestypes() {
		return attachrestypes;
	}

	public void setAttachrestypes(String[] attachrestypes) {
		this.attachrestypes = attachrestypes;
	}

	public Integer getDefaultType() {
		return defaultType;
	}

	public void setDefaultType(Integer defaultType) {
		this.defaultType = defaultType;
	}

	public String[] getKpimetric() {
		return kpimetric;
	}

	public void setKpimetric(String[] kpimetric) {
		this.kpimetric = kpimetric;
	}

	public String getEntitytype() {
		return entitytype;
	}

	public void setEntitytype(String entitytype) {
		this.entitytype = entitytype;
	}

	public boolean isOnlyclassify() {
		return onlyclassify;
	}

	public void setOnlyclassify(boolean onlyclassify) {
		this.onlyclassify = onlyclassify;
	}

	public boolean isHasFirm() {
		return hasFirm;
	}

	public void setHasFirm(boolean hasFirm) {
		this.hasFirm = hasFirm;
	}

	public boolean getIsAsset() {
		return isAsset;
	}

	public void setIsAsset(boolean isAsset) {
		this.isAsset = isAsset;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	/**
	 * @return the fullPath
	 */
	public String getFullPath() {
		return fullPath;
	}

	/**
	 * @param fullPath
	 *            the fullPath to set
	 */
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

}
