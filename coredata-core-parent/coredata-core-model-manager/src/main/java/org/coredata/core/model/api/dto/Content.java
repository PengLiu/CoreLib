package org.coredata.core.model.api.dto;

import java.util.List;

public class Content {

	private String title;

	private String id;

	private List<Detail> content;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Detail> getContent() {
		return content;
	}

	public void setContent(List<Detail> content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
