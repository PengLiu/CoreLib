package org.coredata.core.agent.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;

public class Cmd {

	private String seq;

	private String rootId;

	private String cmd;

	private String name;

	private String result;

	private long timeout = 5000; // timeout in ms

	private long period = 0;

	private int retry = 1;

	private State state = State.OK;

	private String errMsg = null;

	private String instanceId;

	private String modelId;

	private List<Long> tasktimes = new ArrayList<>();

	/**
	 * 采集方式，像snmp中的GET或者WALK
	 */
	private String collectType;

	/**
	 * 是否需要异步回调，默认否
	 */
	private boolean isAsyn = false;

	/**
	 * 保存发送者sender
	 */
	private ActorRef sender;

	/**
	 * 保存actor自身
	 */
	private ActorRef self;

	/**
	 * 命令中携带的变量信息
	 */
	private String params;

	/**
	 * 指令结果是否全局使用
	 */
	private boolean isGlobalResult = false;
	/**
	 * 指令是否是可用性指令
	 */
	private boolean isAvailCmd = false;

	/**
	 * 加入监控的实例信息
	 */
	private List<Map<String, String>> subInstanceInfo = new ArrayList<>();

	/**
	 * 更改对应管理状态
	 */
	private Integer adminState;

	//首次运行时间,用于分散任务到没一秒
	private long firstTimestamp;

	public enum State {
		OK, ERR, TIMEOUT, CONNECTION_ERR
	}

	private String protocol = null;

	private Map<String, String> connection = new HashMap<>();

	/**
	 * 采集参数列表
	 */
	private Map<String, Object> paramsMap;
	/**
	 * 创建模型时所在步骤
	 */
	private String step = null;

	public void appendTaskTime(long tasktime) {
		tasktimes.add(tasktime);
	}

	public String getCmd() {
		return cmd;
	}

	public int getTimeInSecond() {
		return Long.valueOf(this.timeout / 1000).intValue();
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public Map<String, String> getConnection() {
		return connection;
	}

	public void setConnection(Map<String, String> connection) {
		this.connection = connection;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public List<Long> getTasktimes() {
		return tasktimes;
	}

	public void setTasktimes(List<Long> tasktimes) {
		this.tasktimes = tasktimes;
	}

	public String getRootId() {
		return rootId;
	}

	public void setRootId(String rootId) {
		this.rootId = rootId;
	}

	public boolean isAsyn() {
		return isAsyn;
	}

	public void setAsyn(boolean isAsyn) {
		this.isAsyn = isAsyn;
	}

	public ActorRef getSender() {
		return sender;
	}

	public void setSender(ActorRef sender) {
		this.sender = sender;
	}

	public ActorRef getSelf() {
		return self;
	}

	public void setSelf(ActorRef self) {
		this.self = self;
	}

	public String getCollectType() {
		return collectType;
	}

	public void setCollectType(String collectType) {
		this.collectType = collectType;
	}

	@Override
	public String toString() {
		return "Cmd [rootId=" + rootId + ", cmd=" + cmd + ", name=" + name + ", result=" + result + ", timeout=" + timeout + ", period=" + period + ", retry="
				+ retry + ", state=" + state + ", errMsg=" + errMsg + ", instanceId=" + instanceId + ", modelId=" + modelId + ", tasktimes=" + tasktimes
				+ ", protocol=" + protocol + ", connection=" + connection + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
		result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cmd other = (Cmd) obj;
		if (cmd == null) {
			if (other.cmd != null)
				return false;
		} else if (!cmd.equals(other.cmd))
			return false;
		if (instanceId == null) {
			if (other.instanceId != null)
				return false;
		} else if (!instanceId.equals(other.instanceId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @return the isGlobalResult
	 */
	public boolean getIsGlobalResult() {
		return isGlobalResult;
	}

	/**
	 * @param isGlobalResult the isGlobalResult to set
	 */
	public void setIsGlobalResult(boolean isGlobalResult) {
		this.isGlobalResult = isGlobalResult;
	}

	public boolean isAvailCmd() {
		return isAvailCmd;
	}

	public void setAvailCmd(boolean isAvailCmd) {
		this.isAvailCmd = isAvailCmd;
	}

	/**
	 * @return the subInstanceInfo
	 */
	public List<Map<String, String>> getSubInstanceInfo() {
		return subInstanceInfo;
	}

	/**
	 * @param subInstanceInfo the subInstanceInfo to set
	 */
	public void setSubInstanceInfo(List<Map<String, String>> subInstanceInfo) {
		this.subInstanceInfo = subInstanceInfo;
	}

	public Map<String, Object> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, Object> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public Integer getAdminState() {
		return adminState;
	}

	public void setAdminState(Integer adminState) {
		this.adminState = adminState;
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

	public long getFirstTimestamp() {
		return firstTimestamp;
	}

	public void setFirstTimestamp(long firstTimestamp) {
		this.firstTimestamp = firstTimestamp;
	}

}