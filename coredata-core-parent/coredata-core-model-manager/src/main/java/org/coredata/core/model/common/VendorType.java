package org.coredata.core.model.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 厂商类型对应实体，用于保存相关厂商对应的不同版本类型
 * @author sushi
 *
 */
public class VendorType {

	private String id;

	/**
	 * 对应资产类型
	 */
	private String restype;

	/**
	 * 对应下级子厂商及不同版本
	 */
	private List<VendorFirm> firms = new ArrayList<>();

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

	public String getRestype() {
		return restype;
	}

	public void setRestype(String restype) {
		this.restype = restype;
	}

	public List<VendorFirm> getFirms() {
		return firms;
	}

	public void setFirms(List<VendorFirm> firms) {
		this.firms = firms;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
