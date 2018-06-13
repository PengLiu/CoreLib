package org.coredata.core.metric.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.util.StringUtils;

@Document(indexName = "logfile_#{@indexUtils.daySuffix()}", type = "logs", shards = 5, replicas = 1, refreshInterval = "5s")
public class LogFile {

	@Id
	private String id;

	@Field(type = FieldType.Date)
	private long createdTime;

	@Field(type = FieldType.text)
	private String log;

	@Field(type = FieldType.keyword)
	private String filePath;

	@Field(type = FieldType.Ip)
	private String serverIp;

	@Field(type = FieldType.keyword)
	private String logId;

	@Field(type = FieldType.text)
	private String keywords;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getLogId() {
		if (StringUtils.isEmpty(this.logId)) {
			this.logId = this.serverIp + "_" + this.filePath;
		}
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

}