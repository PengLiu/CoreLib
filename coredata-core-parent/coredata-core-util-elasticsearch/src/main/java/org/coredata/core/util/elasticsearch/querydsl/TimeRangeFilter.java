package org.coredata.core.util.elasticsearch.querydsl;

public class TimeRangeFilter {

	private String field = "createdTime";

	private long startMs;

	private long endMs;

	public TimeRangeFilter() {

	}

	public TimeRangeFilter(long startMs, long endMs) {
		this.startMs = startMs;
		this.endMs = endMs;
	}

	public TimeRangeFilter(long startMs, long endMs, String field) {
		this.startMs = startMs;
		this.endMs = endMs;
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public long getStartMs() {
		return startMs;
	}

	public void setStartMs(long startMs) {
		this.startMs = startMs;
	}

	public long getEndMs() {
		return endMs;
	}

	public void setEndMs(long endMs) {
		this.endMs = endMs;
	}

}
