package org.coredata.core.olap.model.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "userinfo", type = "user", shards = 5, replicas = 1, refreshInterval = "5s")
public class UserInfo {

	@Id
	private String id;

	@Field(type = FieldType.keyword)
	private String userName;

	@Field(type = FieldType.keyword)
	private String gender;

	@Field(type = FieldType.Integer)
	private int age;

	@Field(type = FieldType.keyword)
	private String wristbandId;

	@Field(type = FieldType.keyword)
	private String expensesId;
	
	@Field(type = FieldType.keyword)
	private String location;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getWristbandId() {
		return wristbandId;
	}

	public void setWristbandId(String wristbandId) {
		this.wristbandId = wristbandId;
	}

	public String getExpensesId() {
		return expensesId;
	}

	public void setExpensesId(String expensesId) {
		this.expensesId = expensesId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
