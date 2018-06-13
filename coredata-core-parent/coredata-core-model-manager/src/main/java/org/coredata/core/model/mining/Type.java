package org.coredata.core.model.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

public class Type implements Serializable {

	private static final long serialVersionUID = -1170332555124844873L;

	private String method;

	@PersistEncrypted
	private List<Expression> exp = new ArrayList<>();

	private String period;//取多长时间周期内的数据

	private String interval;//时间间隔

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public List<Expression> getExp() {
		return exp;
	}

	public void setExp(List<Expression> exp) {
		this.exp = exp;
	}

}
