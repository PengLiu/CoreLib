package org.coredata.core.agent.collector.result;

public class CmdDataResult {
	private boolean success;

	private String customerId;

	private String nodeId;

	private String modelId;

	private String err;

	private String instanceId;

	private String name;

	private String msg;

	private String type;

	private long tasktime;

	private long finishTime = System.currentTimeMillis();

	/**
	 * 命令中所携带的变量
	 */
	private String params;

	/**
	 * 实例索引index
	 */
	private String index;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public long getTasktime() {
		return tasktime;
	}

	public void setTasktime(long tasktime) {
		this.tasktime = tasktime;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	@Override
	public String toString() {
		return "Result [success=" + success + ", nodeId=" + nodeId + ", modelId=" + modelId + ", err=" + err + ", instanceId=" + instanceId + ", name=" + name
				+ ", msg=" + msg + ", tasktime=" + tasktime + ", finishTime=" + finishTime + "]";
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}