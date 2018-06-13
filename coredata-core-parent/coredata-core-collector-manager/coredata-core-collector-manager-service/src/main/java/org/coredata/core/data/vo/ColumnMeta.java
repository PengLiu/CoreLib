package org.coredata.core.data.vo;

public class ColumnMeta {

	public ColumnMeta() {
	}

	private int index;

	private String name;

	private String type;

	private String comment;

	private int size;

	private boolean isUse;

	private int parentIndex;

	private boolean isUseByInstId;

	private boolean isUseByInstName;

	public ColumnMeta(String name, String type, String comment) {
		this.name = name;
		this.type = type;
		this.comment = comment;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public boolean isUse() {
		return isUse;
	}

	public void setUse(boolean isUse) {
		this.isUse = isUse;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getParentIndex() {
		return parentIndex;
	}

	public void setParentIndex(int parentIndex) {
		this.parentIndex = parentIndex;
	}

	public boolean isUseByInstId() {
		return isUseByInstId;
	}

	public void setUseByInstId(boolean isUseByInstId) {
		this.isUseByInstId = isUseByInstId;
	}

	public boolean isUseByInstName() {
		return isUseByInstName;
	}

	public void setUseByInstName(boolean isUseByInstName) {
		this.isUseByInstName = isUseByInstName;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
