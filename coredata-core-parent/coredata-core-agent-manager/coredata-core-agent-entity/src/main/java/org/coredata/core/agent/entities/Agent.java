package org.coredata.core.agent.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class Agent implements Serializable, Delayed {

	private static final long serialVersionUID = -8077653536981347180L;

	@Transient
	private long expire;

	@Transient
	private long delay = 3 * 60 * 1000;

	/**
	 * 主键id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Agent类型，主要有standard|embedded|light
	 */
	private String type;

	/**
	 * Agent所在ip地址
	 */
	private String ipAddress;

	/**
	 * 记录创建时间
	 */
	private long createTime;

	/**
	 * 最近一次上线时间
	 */
	private long lastOnlineTime;

	/**
	 * 最近一次离线时间
	 */
	private long lastOfflineTime;

	/**
	 * 在线状态，默认离线
	 */
	private int status = 0;

	/**
	 * Agent信息描述
	 */
	private String info;

	/**
	 * 该Agent可以完成的功能
	 */
	private String features;

	private String token;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<AgentTask> taskes = new HashSet<>();

	public void addTask(AgentTask task) {
		taskes.add(task);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getLastOnlineTime() {
		return lastOnlineTime;
	}

	public void setLastOnlineTime(long lastOnlineTime) {
		this.lastOnlineTime = lastOnlineTime;
	}

	public long getLastOfflineTime() {
		return lastOfflineTime;
	}

	public void setLastOfflineTime(long lastOfflineTime) {
		this.lastOfflineTime = lastOfflineTime;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof Agent) {
			if (id == null)
				return false;
			else
				return id.equals(((Agent) another).getId());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return id == null ? -1 : id.hashCode();
	}

	public void updateDeatTime() {
		this.expire = System.currentTimeMillis() + delay;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(this.expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
	}

	public Set<AgentTask> getTaskes() {
		return taskes;
	}

	public void setTaskes(Set<AgentTask> taskes) {
		this.taskes = taskes;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
