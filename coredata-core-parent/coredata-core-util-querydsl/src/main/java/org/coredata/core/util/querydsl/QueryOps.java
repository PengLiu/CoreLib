package org.coredata.core.util.querydsl;

public enum QueryOps {

	lt("<"), lte("<="), gt(">"), gte(">="), eq("="), like("like"), prefix("prefix");

	private final String name;

	private QueryOps(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}

}
