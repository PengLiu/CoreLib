package org.coredata.core.olap.model.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "expenses_record", type = "expensesRecord", shards = 5, replicas = 1, refreshInterval = "5s")
public class ExpensesRecord {

	@Id
	private String id;

	@Field(type = FieldType.Double)
	private double amount;

	@Field(type = FieldType.keyword)
	private String type;

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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
