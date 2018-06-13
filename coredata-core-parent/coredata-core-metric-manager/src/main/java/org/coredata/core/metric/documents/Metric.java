package org.coredata.core.metric.documents;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.util.StringUtils;

@Document(indexName = "metric_#{@indexUtils.daySuffix()}", type = "metric", shards = 5, replicas = 1, refreshInterval = "5s")
public class Metric {

	public static final String timeFieldId = "createdTime";

	public static final String entityFieldId = "entityId";

	public static final String metricFieldId = "metricId";
	
	public static final String rollUpFieldId = "rollupId";

	public static final String valFieldId = "val";

	public static final String ALG_AVG = "avg";
	public static final String ALG_MAX = "max";
	public static final String ALG_MIN = "min";

	@Id
	private String id;

	@Field(type = FieldType.keyword)
	private String metricId;

	@Field(type = FieldType.Boolean, index = false, store = false)
	private Boolean boolVal;

	@Field(type = FieldType.Double, index = false, store = false)
	private Double val;

	@Field(type = FieldType.keyword, index = false, store = false)
	private String stringVal;

	@Field(type = FieldType.keyword)
	private String entityId;

	@Field(type = FieldType.Date)
	private long createdTime = System.currentTimeMillis();

	@Field(type = FieldType.keyword)
	private String token;

	@Field(type = FieldType.keyword)
	private String rollupId;

	/**
	 * 增加决策模型
	 */
	@Persistent
	private String decisionModel;

	public Metric() {

	}

	public Metric(String id, String metricId, Boolean boolVal, Double val, String stringVal, String entityId, long createdTime, String token) {
		this.id = id;
		this.metricId = metricId;
		this.boolVal = boolVal;
		this.val = val;
		this.stringVal = stringVal;
		this.entityId = entityId;
		this.createdTime = createdTime;
		this.token = token;
	}

	public static Metric inst(Map<String, Object> metric) {
		return new Metric((String) metric.get("id"), (String) metric.get("metricId"), (Boolean) metric.get("boolVal"), (Double) metric.get("val"),
				(String) metric.get("stringVal"), (String) metric.get("entityId"), Long.parseLong(metric.get("createdTime").toString()),
				(String) metric.get("token"));
	}

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

	public Boolean getBoolVal() {
		return boolVal;
	}

	public void setBoolVal(Boolean boolVal) {
		this.boolVal = boolVal;
	}

	public String getStringVal() {
		return stringVal;
	}

	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
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

	public Double getVal() {
		return val;
	}

	public void setVal(Double val) {
		this.val = val;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getDecisionModel() {
		return decisionModel;
	}

	public void setDecisionModel(String decisionModel) {
		this.decisionModel = decisionModel;
	}

	public String getRollupId() {
		if (StringUtils.isEmpty(this.rollupId)) {
			this.rollupId = this.token + "_" + this.entityId + "_" + this.metricId;
		}
		return rollupId;
	}

	public void setRollupId(String rollupId) {
		this.rollupId = rollupId;
	}

}
