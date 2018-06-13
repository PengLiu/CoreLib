package org.coredata.core.model.discovery;

import org.coredata.core.model.collection.Param;
import org.coredata.core.util.encryption.PersistEncrypted;

import java.util.ArrayList;
import java.util.List;

public class Datasource {

	private String id;

	@PersistEncrypted
	private String cmd;

	private String type;

	private List<Param> param = new ArrayList<>();

	private String withheader;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Param> getParam() {
		return param;
	}

	public void setParam(List<Param> param) {
		this.param = param;
	}

	public String getWithheader() {
		return withheader;
	}

	public void setWithheader(String withheader) {
		this.withheader = withheader;
	}

}
