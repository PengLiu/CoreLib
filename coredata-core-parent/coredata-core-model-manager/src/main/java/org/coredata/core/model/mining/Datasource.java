package org.coredata.core.model.mining;

import java.io.Serializable;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 数据挖掘的数据源
 * @author sushiping
 *
 */
public class Datasource implements Serializable {

	private static final long serialVersionUID = -7859112678222063952L;

	private String id;

	@PersistEncrypted
	private String sourceres;

	@PersistEncrypted
	private String sourcecmd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSourceres() {
		return sourceres;
	}

	public void setSourceres(String sourceres) {
		this.sourceres = sourceres;
	}

	public String getSourcecmd() {
		return sourcecmd;
	}

	public void setSourcecmd(String sourcecmd) {
		this.sourcecmd = sourcecmd;
	}

}
