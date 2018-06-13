package org.coredata.core.model.common;

import java.io.Serializable;

public class DevtypeModel implements Serializable {

	private static final long serialVersionUID = 7837286782378038298L;

	private String id;//设置主键id

	private String devtypeid;

	private String vendorid;

	private String series;

	private String number;

	private String sysobjectid;

	private String resmodelid;

	/**
	 * 是否系统默认模型，默认是
	 */
	private int isSystem = 1;

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getSysobjectid() {
		return sysobjectid;
	}

	public void setSysobjectid(String sysobjectid) {
		this.sysobjectid = sysobjectid;
	}

	public String getResmodelid() {
		return resmodelid;
	}

	public void setResmodelid(String resmodelid) {
		this.resmodelid = resmodelid;
	}

	public String getDevtypeid() {
		return devtypeid;
	}

	public void setDevtypeid(String devtypeid) {
		this.devtypeid = devtypeid;
	}

	public String getVendorid() {
		return vendorid;
	}

	public void setVendorid(String vendorid) {
		this.vendorid = vendorid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
