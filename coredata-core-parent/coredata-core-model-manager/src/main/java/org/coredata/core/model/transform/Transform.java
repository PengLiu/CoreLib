package org.coredata.core.model.transform;

import java.io.Serializable;

import org.coredata.core.util.encryption.PersistEncrypted;

public class Transform implements Serializable {

	private static final long serialVersionUID = -6640745371824598286L;

	private String name;

	private Datasource datasource;

	@PersistEncrypted
	private String[] filter;

	private Metadata metadata;

	/**
	 * 是否持久化数据
	 */
	private String persistence;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Datasource getDatasource() {
		return datasource;
	}

	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public String getPersistence() {
		return persistence;
	}

	public void setPersistence(String persistence) {
		this.persistence = persistence;
	}

	public String[] getFilter() {
		return filter;
	}

	public void setFilter(String[] filter) {
		this.filter = filter;
	}

}
