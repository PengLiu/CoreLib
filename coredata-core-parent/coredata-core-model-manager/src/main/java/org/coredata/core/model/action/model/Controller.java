package org.coredata.core.model.action.model;

import org.coredata.core.model.collection.Param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Controller implements Serializable {

	private static final long serialVersionUID = 2965597417662146089L;

	private String id;

	private String name;

	private String cmd;

	private List<Param> param = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public List<Param> getParam() {
		return param;
	}

	public void setParam(List<Param> param) {
		this.param = param;
	}

}
