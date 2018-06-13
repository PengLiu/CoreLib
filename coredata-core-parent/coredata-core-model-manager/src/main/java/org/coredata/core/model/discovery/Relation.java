package org.coredata.core.model.discovery;

import java.io.Serializable;

public class Relation implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2088523668853834827L;

	private String src;

	private String target;

	private String relship;

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getRelship() {
		return relship;
	}

	public void setRelship(String relship) {
		this.relship = relship;
	}

}