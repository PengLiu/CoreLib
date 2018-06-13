package org.coredata.core.olap.model.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class OlapModel {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	private String id;
	// 模型名称
	private String name;
	// 业务主题
	private String topic;
	// 描述
	private String description;
	// 关联的任务id
	private String jobId;
	// 主体对象
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private OlapFactIndex factIndex;
	// 创建时间
	private long createdTime = System.currentTimeMillis();

	// 数据更新时间
	private long updateTime;

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public OlapFactIndex getFactIndex() {
		return factIndex;
	}

	public void setFactIndex(OlapFactIndex factIndex) {
		this.factIndex = factIndex;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

}