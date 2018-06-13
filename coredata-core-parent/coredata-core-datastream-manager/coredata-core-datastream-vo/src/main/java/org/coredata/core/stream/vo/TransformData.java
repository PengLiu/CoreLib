package org.coredata.core.stream.vo;

import java.io.Serializable;

import org.coredata.core.model.mining.DataminingModel;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 用于清洗后输出结果
 *
 * @author sushiping
 *
 */
public class TransformData implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 2610365409952897347L;

	private String errMsg;

	private boolean error;

	private String modelid;

	private String customerId;

	private String type;

	/**
	 * 命令名
	 */
	private String name;

	/**
	 * 根节点ID
	 */
	private String nodeId;

	/**
	 * 实例化id
	 */
	private String instanceId;

	/**
	 * 实例索引
	 */
	private String index;

	private JsonNode resultJson;

	/**
	 * 原始JSON字符串
	 */
	private String msg;

	private String result;
	/**
	 * 任务开始时间
	 */
	private long tasktime = 0;

	/**
	 * 任务完成时间
	 */
	private long finishTime = 0;

	/**
	 * 携带的变量信息
	 */
	private String params;

	/**
	 * 挖掘模型
	 */
	private DataminingModel dataminingModel;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
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

	public String getModelid() {
		return modelid;
	}

	public void setModelid(String modelid) {
		this.modelid = modelid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	//	public JsonNode getResult() {
	//		return result;
	//	}
	//
	//	public void setResult(JsonNode result) {
	//		this.result = result;
	//	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "TransformData [errMsg=" + errMsg + ", error=" + error + ", modelid=" + modelid + ", name=" + name + ", nodeId=" + nodeId + ", instanceId="
				+ instanceId + ", result=" + result + ", tasktime=" + tasktime + ", finishTime=" + finishTime + ", params=" + params + "]";
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
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	public DataminingModel getDataminingModel() {
		return dataminingModel;
	}

	public void setDataminingModel(DataminingModel dataminingModel) {
		this.dataminingModel = dataminingModel;
	}

	public JsonNode getResultJson() {
		return resultJson;
	}

	public void setResultJson(JsonNode resultJson) {
		this.resultJson = resultJson;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

}
