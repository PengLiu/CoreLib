package org.coredata.core.util.querydsl;

public enum Ops {

	and("&"), or("|"), lt("<"), lte("<="), gt(">"), gte(">="), eq("="), neq("<>"), in("in"), like("like"), prefix("prefix"), contains("contains"), notcontains(
			"notcontains"), isnull("isnull"), notnull("notnull");

	private final String name;

	private Ops(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}

}
