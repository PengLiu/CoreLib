package org.coredata.core.model.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.coredata.ntm.api.If;
import com.coredata.ntm.api.IpAddr;

/**
 * 用于保存ntm发现的接口表对象
 * @author sushi
 *
 */
public class NtmNodeDto {

	/**
	 * 表明拓扑发现的节点id，默认拓扑时传入
	 */
	private Long nodeId;

	/**
	 * 对应接口索引
	 */
	@JSONField(name = "INDEX")
	private String index;

	/**
	 * 对应实例id，此属性可能不存在，需要自己实现
	 */
	@JSONField(name = "INSTID")
	private String instId;

	/**
	 * 对应接口名称
	 */
	@JSONField(name = "NAME")
	private String name;

	/**
	 * 是否加入监控，仅仅判定status状态即可
	 */
	@JSONField(name = "ISMONITOR")
	private boolean isMonitor = true;

	/**
	 * 接口描述
	 */
	@JSONField(name = "DESCRIBE")
	private String describe;

	/**
	 * 接口的mac地址
	 */
	@JSONField(name = "MACADDRESS")
	private String macaddress;

	/**
	 * 接口显示名称
	 */
	@JSONField(name = "DISPLAYNAME")
	private String displayName;

	/**
	 * 接口带宽
	 */
	private Long bandwidth;

	/**
	 * 字符串类型的接口带宽，默认进行计算
	 */
	@JSONField(name = "BANDWIDTH")
	private String nicWidth;

	/**
	 * 设置接口ip
	 */
	@JSONField(name = "IP")
	private String ip;

	/**
	 * 设置接口IP地址子网掩码
	 */
	@JSONField(name = "IPADENTNETMASK")
	private String ipAdEntNetMask;

	/**
	 * 接口类型
	 */
	private Integer ifType;

	public NtmNodeDto(If ifTable, IpAddr ipAddr) {
		this.index = String.valueOf(ifTable.getIfIndex());//设置索引
		this.displayName = ifTable.getIfName();//接口显示名称
		this.name = ifTable.getIfName();//接口名称
		this.isMonitor = ifTable.getStatus() == null ? false : ifTable.getStatus().equals(1);//是否加入监控
		this.describe = ifTable.getIfDescr();//设置接口描述
		this.bandwidth = ifTable.getIfSpeed();//设置接口带宽
		this.macaddress = ifTable.getIfPhysAddr();//设置接口Mac地址
		this.ifType = ifTable.getIfType();//设置接口类型
		if (this.bandwidth != null)
			this.nicWidth = this.bandwidth * 1000 * 1000 + "-" + this.bandwidth * 1000 * 1000;
		this.nodeId = ifTable.getId();
		if (ipAddr != null) {
			this.ip = ipAddr.getIpAddress();
			this.ipAdEntNetMask = ipAddr.getNetmask();
		}
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getInstId() {
		return instId;
	}

	public void setInstId(String instId) {
		this.instId = instId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMonitor() {
		return isMonitor;
	}

	public void setMonitor(boolean isMonitor) {
		this.isMonitor = isMonitor;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIpAdEntNetMask() {
		return ipAdEntNetMask;
	}

	public void setIpAdEntNetMask(String ipAdEntNetMask) {
		this.ipAdEntNetMask = ipAdEntNetMask;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getMacaddress() {
		return macaddress;
	}

	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}

	public Long getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Long bandwidth) {
		this.bandwidth = bandwidth;
	}

	public Integer getIfType() {
		return ifType;
	}

	public void setIfType(Integer ifType) {
		this.ifType = ifType;
	}

}
