package org.coredata.core.vo;

import java.util.List;

public class DataImportSearchCondition {
	private String name;

	private List<String> type;

	private String token;

	private List<String> dataSourceType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getType() {
		return type;
	}

	public void setType(List<String> type) {
		this.type = type;
	}

	public List<String> getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(List<String> dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
