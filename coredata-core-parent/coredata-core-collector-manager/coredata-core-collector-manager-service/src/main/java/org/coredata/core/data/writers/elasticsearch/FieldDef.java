package org.coredata.core.data.writers.elasticsearch;

public class FieldDef {

	private String name;

	private String type;

	private String comment;
	public FieldDef(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public FieldDef(String name, String type, String comment) {
		this.name = name;
		this.type = type;
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

}
