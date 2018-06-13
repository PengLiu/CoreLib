package org.coredata.core.stream.alarm;

public class AlarmVO {

	private String content;

	private int level;

	private String instId;

	private long createdTime = System.currentTimeMillis();

	private String expId;

	private String metric;

	private String metricVal;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getInstId() {
		return instId;
	}

	public void setInstId(String instId) {
		this.instId = instId;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getExpId() {
		return expId;
	}

	public void setExpId(String expId) {
		this.expId = expId;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getMetricVal() {
		return metricVal;
	}

	public void setMetricVal(String metricVal) {
		this.metricVal = metricVal;
	}

}
