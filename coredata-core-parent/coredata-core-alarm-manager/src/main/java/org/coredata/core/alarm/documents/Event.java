package org.coredata.core.alarm.documents;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "event-#{@indexUtils.daySuffix()}", type = "event", shards = 5, replicas = 1, refreshInterval = "5s")
public class Event {

	@Id
	private String eventId = UUID.randomUUID().toString();

	@Field(type = FieldType.keyword)
	private EventType eventType;

	@Field(type = FieldType.keyword)
	private String entityId;

	@Field(type = FieldType.keyword)
	private String metricId;

	@Field(type = FieldType.keyword)
	private String token;

	@Field(type = FieldType.Long)
	private long createdTime = System.currentTimeMillis();

	@Field(type = FieldType.text)
	private String content;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getMetricId() {
		return metricId;
	}

	public void setMetricId(String metricId) {
		this.metricId = metricId;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}