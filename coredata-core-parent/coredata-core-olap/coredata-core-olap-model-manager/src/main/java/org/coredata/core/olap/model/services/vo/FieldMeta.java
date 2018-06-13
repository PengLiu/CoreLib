package org.coredata.core.olap.model.services.vo;

public class FieldMeta {

	private String fieldName;

	private String type;

	private boolean canAggregation;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isCanAggregation() {
		return canAggregation;
	}

	public void setCanAggregation(boolean canAggregation) {
		this.canAggregation = canAggregation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "FieldMeta [fieldName=" + fieldName + ", type=" + type + ", canAggregation=" + canAggregation + "]";
	}

}