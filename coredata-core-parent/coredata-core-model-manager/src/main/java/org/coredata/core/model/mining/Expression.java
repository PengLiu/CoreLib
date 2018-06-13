package org.coredata.core.model.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

public class Expression implements Serializable {

	private static final long serialVersionUID = 2104801271193489608L;

	@PersistEncrypted
	private String metric;

	@PersistEncrypted
	private List<Param> param = new ArrayList<>();

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public List<Param> getParam() {
		return param;
	}

	public void setParam(List<Param> param) {
		this.param = param;
	}

}
