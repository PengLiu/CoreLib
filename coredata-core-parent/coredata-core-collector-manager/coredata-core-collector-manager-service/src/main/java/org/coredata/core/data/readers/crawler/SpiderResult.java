package org.coredata.core.data.readers.crawler;

import java.util.List;

public class SpiderResult {
	private String url;
	private List<SpiderVal> fields;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<SpiderVal> getFields() {
		return fields;
	}

	public void setFields(List<SpiderVal> fields) {
		this.fields = fields;
	}

}
