package org.coredata.core.framework.agentmanager.dto;

/**
 * 该对象用于传递接受实例化相关参数
 * @author sushi
 *
 */
public class InstanceDto {

	/**
	 * 协议名称
	 */
	private String protocol;

	/**
	 * 命令名称，也可以认定为datasoure的key值
	 */
	private String name;

	/**
	 * 针对结果是否需要保留表头，yes|no
	 */
	private String withheader;

	/**
	 * 采集方式，针对snmp，GET|WALK
	 */
	private String collectType;

	/**
	 * 该cmd属于资源类型，root|multiple
	 */
	private String type;

	/**
	 * 该命令的cmd内容
	 */
	private String cmd;

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

	public String getWithheader() {
		return withheader;
	}

	public void setWithheader(String withheader) {
		this.withheader = withheader;
	}

	public String getCollectType() {
		return collectType;
	}

	public void setCollectType(String collectType) {
		this.collectType = collectType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

}
