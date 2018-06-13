package org.coredata.core.framework.agentmanager.cmds.response;

import org.coredata.core.framework.agentmanager.cmds.ActionCmd;
import org.coredata.core.model.constants.ApiConstant;

import java.io.Serializable;

/**
 * 采集命令响应
 * @author sushiping
 *
 */
public class ActionResponse implements Serializable {

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

	/**
	 * 失败原因
	 */
	private String failreson;

	public ActionResponse() {

	}

	public ActionResponse(ActionCmd cmd, String result) {
		this.seq = cmd.getSeq();
		this.action = cmd.getAction();
		this.result = ApiConstant.FAIL_FLAG;
		this.failreson = result;
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

	public String getFailreson() {
		return failreson;
	}

	public void setFailreson(String failreson) {
		this.failreson = failreson;
	}

}
