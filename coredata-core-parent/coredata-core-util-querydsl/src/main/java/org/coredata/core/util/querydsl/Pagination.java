package org.coredata.core.util.querydsl;

public class Pagination {

	private int size = 10;

	private int page = 1;

	public Pagination() {

	}

	public Pagination(int size, int page) {
		this.size = size;
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

}
