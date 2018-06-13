package org.coredata.core.model.mining;

import java.io.Serializable;

/**
 * 数据挖掘模型metadata属性
 * @author sushiping
 *
 */
public class Field implements Serializable {

	private static final long serialVersionUID = -1476552624043864735L;

	private String name;

	private String type;

	private String unit;

	private String desc;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
