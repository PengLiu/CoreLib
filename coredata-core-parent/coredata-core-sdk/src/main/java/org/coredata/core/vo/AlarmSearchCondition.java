package org.coredata.core.vo;

import java.util.Map;

import org.coredata.util.query.Fuzzy;
import org.coredata.util.query.Operation;

public class AlarmSearchCondition {
	private Map<String, Object> searchCondition;
	private long startTime;
	private long endTime;
	private Operation operation;
	private Fuzzy fuzzy;
	private int page;
	private int pageSize;
	private String sortField;
	private String sort;
	public Map<String, Object> getSearchCondition() {
		return searchCondition;
	}
	public void setSearchCondition(Map<String, Object> searchCondition) {
		this.searchCondition = searchCondition;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public Fuzzy getFuzzy() {
		return fuzzy;
	}
	public void setFuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	

}
