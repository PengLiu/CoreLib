package org.coredata.core.vo;

public class ElasticSearchCondition {
	private String query;
	private int fetch_size = 100;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getFetch_size() {
		return fetch_size;
	}

	public void setFetch_size(int fetch_size) {
		this.fetch_size = fetch_size;
	}

}
