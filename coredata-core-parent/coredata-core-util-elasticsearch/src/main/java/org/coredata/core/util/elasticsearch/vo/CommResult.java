package org.coredata.core.util.elasticsearch.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class CommResult {

	private long total;

	private long used;

	private List<String> records = new ArrayList<>();

	private Map<String, JsonNode> aggregations = new HashMap<>();

	public void addAggregationResult(String key, JsonNode agg) {
		aggregations.put(key, agg);
	}

	public void addRecord(String record) {
		records.add(record);
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public List<String> getRecords() {
		return records;
	}

	public void setRecords(List<String> records) {
		this.records = records;
	}

	public Map<String, JsonNode> getAggregations() {
		return aggregations;
	}

	public void setAggregations(Map<String, JsonNode> aggregations) {
		this.aggregations = aggregations;
	}

	@Override
	public String toString() {
		return "CommResult [total=" + total + ", used=" + used + ", records=" + records + ", aggregations=" + aggregations + "]";
	}

}
