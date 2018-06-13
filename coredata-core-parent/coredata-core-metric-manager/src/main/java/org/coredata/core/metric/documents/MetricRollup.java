package org.coredata.core.metric.documents;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "metric_rollup_#{@indexUtils.daySuffix()}", type = "metric", shards = 5, replicas = 1, refreshInterval = "5s")
public class MetricRollup {

	@Id
	private String id = UUID.randomUUID().toString();

	@Field(type = FieldType.keyword)
	private String metricId;

	@Field(type = FieldType.keyword)
	private String entityId;

	@Field(type = FieldType.Date)
	private long createdTime = System.currentTimeMillis();

	@Field(type = FieldType.keyword)
	private String token;

	@Field(type = FieldType.Long, index = false, store = false)
	private Long count;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double min;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double avg;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double max;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double sum;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double sumOfSquares;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double variance;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double stdDeviation;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMetricId() {
		return metricId;
	}

	public void setMetricId(String metricId) {
		this.metricId = metricId;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getAvg() {
		return avg;
	}

	public void setAvg(Double avg) {
		this.avg = avg;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	public Double getSumOfSquares() {
		return sumOfSquares;
	}

	public void setSumOfSquares(Double sumOfSquares) {
		this.sumOfSquares = sumOfSquares;
	}

	public Double getStdDeviation() {
		return stdDeviation;
	}

	public void setStdDeviation(Double stdDeviation) {
		this.stdDeviation = stdDeviation;
	}

	public Double getVariance() {
		return variance;
	}

	public void setVariance(Double variance) {
		this.variance = variance;
	}

}
