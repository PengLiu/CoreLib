package org.coredata.core.framework.coremanager.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class InstanceCallback {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final String COMMON = ",";

	protected static final String SPLITER_POINT = "[.]";

	/**
	 * 前台传入的seq号
	 */
	protected String seq;

	protected Map<String, String> resp = new HashMap<>();

	public abstract void process(String result);

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

}
