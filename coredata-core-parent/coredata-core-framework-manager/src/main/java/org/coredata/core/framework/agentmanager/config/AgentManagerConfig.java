package org.coredata.core.framework.agentmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AgentManagerConfig {

	@Value("${task.needretry}")
	private boolean needRetry = false;

	@Value("${task.retrytime}")
	private int retryTime = 10;

	@Value("${task.period}")
	private String period = null;

	@Value("${agent.disconnect.timeout}")
	private long agentDisconnectTimeout = 5000;

	public boolean isNeedRetry() {
		return needRetry;
	}

	public void setNeedRetry(boolean needRetry) {
		this.needRetry = needRetry;
	}

	public int getRetryTime() {
		return retryTime;
	}

	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public long getAgentDisconnectTimeout() {
		return agentDisconnectTimeout;
	}

	public void setAgentDisconnectTimeout(long agentDisconnectTimeout) {
		this.agentDisconnectTimeout = agentDisconnectTimeout;
	}

}
