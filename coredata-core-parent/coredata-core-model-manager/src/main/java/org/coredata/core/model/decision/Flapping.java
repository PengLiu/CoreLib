package org.coredata.core.model.decision;

import java.io.Serializable;

public class Flapping implements Serializable {

	private static final long serialVersionUID = 6643286800099196977L;

	private int count = 3;

	private String type = "consecutive";

	private String frequency = "30m";

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

}
