package org.coredata.core.framework.agentmanager.cmds.response;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class InstanceResponse implements Serializable {

    private static final long serialVersionUID = 0x3e5476ad5495c3c8L;

	/**
	 * 响应返回结果
	 */
	private List<Map<String, Object>> results;

	/**
	 * 响应此次请求类型
	 */
	private String action;

	/**
	 * 记录Agent请求携带上来的id，用于返回给Agent时匹配对应的命令
	 */
	private String seq;

	public List<Map<String, Object>> getResults() {
		return results;
	}

	public void setResults(List<Map<String, Object>> results) {
		this.results = results;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

}
