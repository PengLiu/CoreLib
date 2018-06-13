package org.coredata.core.framework.agentmanager.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 保存监控任务列表，该列表主要用于针对监控任务进行分发
 * @author sushi
 *
 */
public class AgentTask implements Serializable {

	private static final long serialVersionUID = 4709605806764618789L;

	/**
	 * 主键id，自增
	 */
	private Long id;

	/**
	 * 保存实例id，表明该条任务包含的实例id
	 */
	private String instanceId;

	/**
	 * 表明该条任务状态，是否在被执行，默认0为未被执行
	 * 0: 待分配Agent, 1: 已分配Agent , 2: 暂停处理
	 */
	private int status = 0;

	/**
	 * 该条任务被执行的agentId
	 */
	private Long agentId;

	/**
	 * 表明该条任务被谁签收的签名
	 */
	private String signature;

	/**
	 * 该条任务协议
	 */
	private String protocol;

	/**
	 * 表明该条任务重试次数，默认重试超过10次就不再进行处理
	 */
	private int retry = 0;

	/**
	 * 任务创建时间
	 */
	private Timestamp createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof AgentTask) {
			if (id == null)
				return false;
			else
				return id.equals(((AgentTask) another).getId());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return id == null ? -1 : id.hashCode();
	}

}
