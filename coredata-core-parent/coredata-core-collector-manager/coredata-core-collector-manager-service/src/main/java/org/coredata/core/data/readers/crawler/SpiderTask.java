package org.coredata.core.data.readers.crawler;

import java.util.List;

public class SpiderTask {
	private String url;
	private String nextpage;
	private List<SpiderField> fields;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getNextpage() {
		return nextpage;
	}

	public void setNextpage(String nextpage) {
		this.nextpage = nextpage;
	}

	public List<SpiderField> getFields() {
		return fields;
	}

	public void setFields(List<SpiderField> fields) {
		this.fields = fields;
	}

}
