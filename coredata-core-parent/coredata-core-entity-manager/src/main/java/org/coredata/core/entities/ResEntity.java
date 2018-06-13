package org.coredata.core.entities;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;

@NodeEntity
public class ResEntity extends CommEntity {

	private static final long serialVersionUID = -2993754157289742472L;

	private String status;

	@Properties(prefix = "conn")
	private Map<String, Object> conn = new HashMap<>();

	private boolean monitor = false;

	private boolean controll = false;

	public void addConn(String key, Object val) {
		conn.put(key, val);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}

	public boolean isControll() {
		return controll;
	}

	public void setControll(boolean controll) {
		this.controll = controll;
	}

	public Map<String, Object> getConn() {
		return conn;
	}

	public void setConn(Map<String, Object> conn) {
		this.conn = conn;
	}

}
