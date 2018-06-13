package org.coredata.core.alarm.documents;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "alarm_#{@indexUtils.daySuffix()}", type = "alarm", shards = 5, replicas = 1, refreshInterval = "5s")
public class Alarm {

	@Id
	private String id;

	@Field(type = FieldType.keyword)
	private String alarmRuleId;

	@Field(type = FieldType.keyword)
	private String token;

	@Field(type = FieldType.Integer)
	private int level;

	@Field(type = FieldType.text)
	private String content;

	@Field(type = FieldType.Date)
	private long createdTime = System.currentTimeMillis();

	@Field(type = FieldType.Nested)
	private List<Event> events;

	@Field(type = FieldType.Nested)
	private List<AlarmSource> alarmSources;

	@Field(type = FieldType.Nested)
	private Map<String, Object> props;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getAlarmRuleId() {
		return alarmRuleId;
	}

	public void setAlarmRuleId(String alarmRuleId) {
		this.alarmRuleId = alarmRuleId;
	}

	public List<AlarmSource> getAlarmSources() {
		return alarmSources;
	}

	public void setAlarmSources(List<AlarmSource> alarmSources) {
		this.alarmSources = alarmSources;
	}

	public static boolean isAttribute(String attName) {
		switch (attName) {
		case "alarmRuleId":
		case "token":
		case "level":
		case "createdTime":
		case "content":
			return true;
		default:
			return false;
		}
	}

}
