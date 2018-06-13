package org.coredata.core.framework.agentmanager.dto;

import java.io.Serializable;

public class ActionDto implements Serializable {

	private static final long serialVersionUID = -6336330944835872556L;

	/**
	 * 传递来的OID
	 */
	private String oid;

	/**
	 * 根资产的instId
	 */
	private String instId;

	/**
	 * 状态开关
	 */
	private Integer state;

	public String getInstId() {
		return instId;
	}

	public void setInstId(String instId) {
		this.instId = instId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

}
