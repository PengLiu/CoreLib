package org.coredata.core.agent.collector.result;

public class CmdStepDataResult {
	
	private String seq;
	
	private boolean success;

	private String customerId;
	
	private String step;

	private String modelId;

	private String err;

	private String name;

	private String msg;

	private String type;
	
	/**
	 * 命令中所携带的变量
	 */
	private String params;

	private long tasktime;

	private long finishTime = System.currentTimeMillis();




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
		return "Result [customerId="+customerId+", step=" + step + ", success=" + success + ",  modelId=" + modelId + ", err=" + err + ",  name=" + name
				+ ", msg=" + msg + ", tasktime=" + tasktime + ", finishTime=" + finishTime + "]";
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
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

	/**
	 * @return the step
	 */
	public String getStep() {
		return step;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(String step) {
		this.step = step;
	}

	/**
	 * @return the seq
	 */
	public String getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}
}