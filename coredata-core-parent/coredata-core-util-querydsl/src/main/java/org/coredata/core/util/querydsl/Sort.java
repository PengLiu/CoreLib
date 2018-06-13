package org.coredata.core.util.querydsl;

public class Sort {

	private Direction direction = Direction.ASC;

	private String[] fields;

	public Sort() {

	}

	public Sort(String[] fields, Direction direction) {
		this.fields = fields;
		this.direction = direction;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String[] getFields() {
		return fields;
	}

	public void setField(String[] fields) {
		this.fields = fields;
	}

}
