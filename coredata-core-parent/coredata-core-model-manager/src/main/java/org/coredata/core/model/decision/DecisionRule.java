package org.coredata.core.model.decision;

import java.io.Serializable;
import java.util.List;

public class DecisionRule implements Serializable {

	private static final long serialVersionUID = -1694204198424385069L;

	private String exp;

	private List<Action> action;

	private boolean enable = false;

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public List<Action> getAction() {
		return action;
	}

	public void setAction(List<Action> action) {
		this.action = action;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

}
