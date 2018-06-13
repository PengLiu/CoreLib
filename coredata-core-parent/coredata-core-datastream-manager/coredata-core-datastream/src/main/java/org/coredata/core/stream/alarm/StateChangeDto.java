package org.coredata.core.stream.alarm;

import java.io.Serializable;
import java.util.List;

public class StateChangeDto implements Serializable {

	private static final long serialVersionUID = -2935569286549518575L;

	/**
	 * 更新状态，green或者red
	 */
	private String state;

	/**
	 * 待更新的资产instId
	 */
	private List<String> instIds;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<String> getInstIds() {
		return instIds;
	}

	public void setInstIds(List<String> instIds) {
		this.instIds = instIds;
	}

}
