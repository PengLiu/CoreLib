package org.coredata.core.stream.vo;

import java.io.Serializable;
import java.util.List;

public class DSInfo implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5211046148164341956L;

	private String id;

	private String alias;

	private String key;

	private List<String> keys;

	private Object value;

	public DSInfo() {

	}
	
	public DSInfo(String id, String alias, String key) {
		this.id = id;
		this.alias = alias;
		this.key = key;
	}

	public DSInfo(String id, String alias, String key, List<String> keys) {
		this.id = id;
		this.alias = alias;
		this.key = key;
		this.keys = keys;
	}

	public String getAlias() {
		return alias;
	}

	public String getKey() {
		return key.toLowerCase();
	}

	public String getId() {
		return id;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	@Override
	public String toString() {
		return "DSInfo [id=" + id + ", alias=" + alias + ", key=" + key + ", value=" + value + "]";
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

}
