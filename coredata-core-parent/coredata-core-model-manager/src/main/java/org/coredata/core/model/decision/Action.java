package org.coredata.core.model.decision;

import java.io.Serializable;
import java.util.List;

public class Action implements Serializable {

	private static final long serialVersionUID = -3474193721350329541L;

	private String type;

	private List<Param> param;

	private String level;

	private boolean enable = true;

	private Flapping flapping;

	private String period;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Param> getParam() {
		return param;
	}

	public void setParam(List<Param> param) {
		this.param = param;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public Flapping getFlapping() {
		return flapping;
	}

	public void setFlapping(Flapping flapping) {
		this.flapping = flapping;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

}
