package org.coredata.core.stream.vo;

import java.io.Serializable;

public class Unit implements Serializable {

	private static final long serialVersionUID = 738838471937093931L;

	private double value;

	private long timestamp;

	public Unit() {

	}

	public Unit(double value, long timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}

	public double getValue() {
		return value;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
