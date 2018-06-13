package org.coredata.core.metric.documents;

import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 公网舆情爬虫数据索引
 *
 */
@Document(indexName = "webopinion_#{@indexUtils.daySuffix()}", type = "opinion", shards = 5, replicas = 1, refreshInterval = "5s")
public class WebOpinion {

	@Id
	private String id = UUID.randomUUID().toString();

	@Field(type = FieldType.keyword)
	private String category;

	@Field(type = FieldType.keyword)
	private String src;

	@Field(type = FieldType.keyword)
	private String url;

	@Field(type = FieldType.text)
	private String title;

	@Field(type = FieldType.text)
	private String content;

	@Field(type = FieldType.text)
	private String keywords;

	@Field(type = FieldType.Integer)
	private int polarity;
	
	@Field(type = FieldType.text)
	private String summary;

	@Field(type = FieldType.Date)
	@JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "yyyy-MM-dd HH:mm")
	private Date createdTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public int getPolarity() {
		return polarity;
	}

	public void setPolarity(int polarity) {
		this.polarity = polarity;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
