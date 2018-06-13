package org.coredata.core.model.decision;

import java.io.Serializable;

public class Associatedres implements Serializable {

	private static final long serialVersionUID = 1973832193744896219L;

	private String[] instid;

	private String[] metric;

	public String[] getInstid() {
		return instid;
	}

	public void setInstid(String[] instid) {
		this.instid = instid;
	}

	public String[] getMetric() {
		return metric;
	}

	public void setMetric(String[] metric) {
		this.metric = metric;
	}

}
