package org.coredata.core.data;

import java.util.List;


public class Data {
    private List<String> header;
    private List<String> rows;
	public List<String> getHeader() {
		return header;
	}
	public void setHeader(List<String> header) {
		this.header = header;
	}
	public List<String> getRows() {
		return rows;
	}
	public void setRows(List<String> rows) {
		this.rows = rows;
	}
}
