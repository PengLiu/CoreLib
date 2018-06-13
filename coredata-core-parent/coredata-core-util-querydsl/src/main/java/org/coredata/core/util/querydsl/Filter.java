package org.coredata.core.util.querydsl;

public class Filter {

	private String field;

	private Ops ops = Ops.eq;

	private Object value;

	public Filter() {
		
	}

	public Filter(String field, Object value, Ops ops) {
		this.ops = ops;
		this.field = field;
		this.value = value;
	}

	public Filter(String field, Object value) {
		this.field = field;
		this.value = value;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Ops getOps() {
		return ops;
	}

	public void setOps(Ops ops) {
		this.ops = ops;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
