package org.coredata.core.vo;

import java.util.Map;

import org.coredata.util.query.AggregationType;
import org.coredata.util.query.Fuzzy;
import org.coredata.util.query.Operation;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

public class AlarmCountConfidtion {

	private Map<String, Object> searchCondition;
	private long startTime;
	private long endTime;
	private AggregationType aggregationType;
	private String[] groupBy;
	private Operation operation;
	private Fuzzy fuzzy;
	private DateHistogramInterval interval;
	private String timeField;

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

	public AggregationType getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(AggregationType aggregationType) {
		this.aggregationType = aggregationType;
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

	public String[] getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String[] groupBy) {
		this.groupBy = groupBy;
	}

	public DateHistogramInterval getInterval() {
		return interval;
	}

	public void setInterval(DateHistogramInterval interval) {
		this.interval = interval;
	}

	public String getTimeField() {
		return timeField;
	}

	public void setTimeField(String timeField) {
		this.timeField = timeField;
	}
	

}
