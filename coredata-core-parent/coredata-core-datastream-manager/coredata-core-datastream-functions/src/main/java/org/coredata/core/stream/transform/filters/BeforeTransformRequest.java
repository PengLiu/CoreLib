package org.coredata.core.stream.transform.filters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 用于处理清洗请求的实体类
 *
 * @author sushiping
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BeforeTransformRequest {

	private String modelid;

	private String nodeId;
	/**
	 * 实例化对应id
	 */
	private String instanceId;

	/**
	 * 采集命令id
	 */
	private String name;

	private JsonNode request;

	/**
	 * 任务执行时间
	 */
	private long tasktime = 0;

	/**
	 * 任务完成时间
	 */
	private long finishTime = 0;

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

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public JsonNode getRequest() {
		return request;
	}

	public void setRequest(JsonNode request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return "BeforeTransformRequest [modelid=" + modelid + ", nodeId=" + nodeId + ", instanceId=" + instanceId
				+ ", name=" + name + ", request=" + request + ", tasktime=" + tasktime + ", finishTime=" + finishTime
				+ "]";
	}

}