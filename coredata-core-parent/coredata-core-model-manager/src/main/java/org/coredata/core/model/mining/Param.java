package org.coredata.core.model.mining;

import java.io.Serializable;

import org.coredata.core.util.encryption.PersistEncrypted;

public class Param implements Serializable {

	private static final long serialVersionUID = -2085091340465329987L;

	@PersistEncrypted
	private String key;

	@PersistEncrypted
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
