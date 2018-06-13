package org.coredata.core.framework.agentmanager.cmds.response;


import org.coredata.core.framework.agentmanager.cmds.DetectCmd;

import java.io.Serializable;

/**
 * 检测对应响应类
 * @author sushiping
 *
 */
public class DetectResponse implements Serializable {

    private static final long serialVersionUID = 0xcd520fe984bdf261L;

	/**
	 * 响应返回结果
	 */
	private String result;

	/**
	 * 响应此次请求类型
	 */
	private String action;

	/**
	 * 记录Agent请求携带上来的id，用于返回给Agent时匹配对应的命令
	 */
	private String seq;

	public DetectResponse() {

	}

	public DetectResponse(DetectCmd cmd, String result) {
		this.result = result;
		this.action = cmd.getAction();
		this.seq = cmd.getSeq();
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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