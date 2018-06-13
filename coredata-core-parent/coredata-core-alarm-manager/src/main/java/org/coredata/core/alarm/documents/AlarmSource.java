package org.coredata.core.alarm.documents;

public class AlarmSource {

	private String entityId;

	private String metricId;

	public AlarmSource() {

	}

	public AlarmSource(String entityId, String metricId) {
		this.entityId = entityId;
		this.metricId = metricId;
	}

	public String getEntityId() {
		return entityId;
	}

	public String getMetricId() {
		return metricId;
	}

}
