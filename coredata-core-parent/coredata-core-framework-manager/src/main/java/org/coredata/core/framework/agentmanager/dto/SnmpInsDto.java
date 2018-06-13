package org.coredata.core.framework.agentmanager.dto;

import java.util.List;
import java.util.Map;

/**
 * 用于实例化snmp传输对象，包含单资源发现及拓扑发现
 * @author sushi
 *
 */
public class SnmpInsDto {

	/**
	 * snmp协议的连接信息
	 */
	private Map<String, String> conn;

	/**
	 * 对应的snmp实例对象
	 */
	private List<InstanceDto> instance;

	/**
	 * 对应实例对象中可能会用到的变量参数
	 */
	private List<Map<String, String>> params;

	public Map<String, String> getConn() {
		return conn;
	}

	public void setConn(Map<String, String> conn) {
		this.conn = conn;
	}

	public List<InstanceDto> getInstance() {
		return instance;
	}

	public void setInstance(List<InstanceDto> instance) {
		this.instance = instance;
	}

	public List<Map<String, String>> getParams() {
		return params;
	}

	public void setParams(List<Map<String, String>> params) {
		this.params = params;
	}

}
