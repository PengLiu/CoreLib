package org.coredata.core.olap.model.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "wristband", type = "wristband", shards = 5, replicas = 1, refreshInterval = "5s")
public class Wristband {

	@Id
	private String id;

	@Field(type = FieldType.Integer)
	private int heartBeat;

	@Field(type = FieldType.Double)
	private double calories;

	@Field(type = FieldType.Integer)
	private int power;

	@Field(type = FieldType.Long)
	private long createdTime = System.currentTimeMillis();

	@Field(type = FieldType.keyword)
	private String srcIndex;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getHeartBeat() {
		return heartBeat;
	}

	public void setHeartBeat(int heartBeat) {
		this.heartBeat = heartBeat;
	}

	public double getCalories() {
		return calories;
	}

	public void setCalories(double calories) {
		this.calories = calories;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getSrcIndex() {
		return srcIndex;
	}

	public void setSrcIndex(String srcIndex) {
		this.srcIndex = srcIndex;
	}

}
