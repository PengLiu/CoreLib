package org.coredata.core.data.writers.elasticsearch;

public class DataCache {

	private String index;

	private String type;

	private String id;

	private String data;

	public DataCache() {

	}

	public DataCache(String index, String type, String data) {
		this.index = index;
		this.type = type;
		this.data = data;
	}

	public DataCache(String index, String type, String id, String data) {
		this.index = index;
		this.type = type;
		this.id = id;
		this.data = data;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DataCache [index=" + index + ", type=" + type + ", id=" + id + ", data=" + data + "]";
	}

}
