package org.coredata.core.data.vo;

public class HDFSMeta {
	
	private String path;
	
	private String fieldSplitter = "\t";
	
	private String lineSplitter = "\n";

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFieldSplitter() {
		return fieldSplitter;
	}

	public void setFieldSplitter(String fieldSplitter) {
		this.fieldSplitter = fieldSplitter;
	}

	public String getLineSplitter() {
		return lineSplitter;
	}

	public void setLineSplitter(String lineSplitter) {
		this.lineSplitter = lineSplitter;
	}
	
}
