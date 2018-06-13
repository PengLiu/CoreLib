package org.coredata.core.stream.mining.entity;

import org.coredata.core.model.mining.Expression;
import org.coredata.core.stream.vo.TransformData;

public class MiningData {

	private TransformData res;
	private MetricInfo info;

	private String aid;
	private Expression exp;
	private String miningId;

	public MiningData() {

	}

	public MiningData(MetricInfo info, TransformData resp, String aid, Expression exp, String miningId) {
		this.info = info;
		this.res = resp;
		this.aid = aid;
		this.exp = exp;
		this.miningId = miningId;
	}

	public TransformData getRes() {
		return res;
	}

	public MetricInfo getInfo() {
		return info;
	}

	public String getAid() {
		return aid;
	}

	public Expression getExp() {
		return exp;
	}

	public String getMiningId() {
		return miningId;
	}

}
