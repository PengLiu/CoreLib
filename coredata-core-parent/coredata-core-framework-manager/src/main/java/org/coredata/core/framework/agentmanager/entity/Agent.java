package org.coredata.core.framework.agentmanager.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Agent实体，包含相关实体属性
 * @author sushi
 *
 */
public class Agent implements Serializable, Delayed {

	private static final long serialVersionUID = -8077653536981347180L;

	private long expire;

	private long delay = 3 * 60 * 1000;

	/**
	 * 主键id
	 */
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
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Timestamp createTime;

	/**
	 * 最近一次上线时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Timestamp lastOnlineTime;

	/**
	 * 最近一次离线时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Timestamp lastOfflineTime;

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

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getLastOnlineTime() {
		return lastOnlineTime;
	}

	public void setLastOnlineTime(Timestamp lastOnlineTime) {
		this.lastOnlineTime = lastOnlineTime;
	}

	public Timestamp getLastOfflineTime() {
		return lastOfflineTime;
	}

	public void setLastOfflineTime(Timestamp lastOfflineTime) {
		this.lastOfflineTime = lastOfflineTime;
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

}
