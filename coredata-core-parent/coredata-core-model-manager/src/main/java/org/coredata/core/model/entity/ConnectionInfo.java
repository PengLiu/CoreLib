package org.coredata.core.model.entity;

import java.util.List;
import java.util.UUID;

/**
 * 连接信息实体对象，用于缓存相关数据
 * @author sushi
 *
 */
public class ConnectionInfo {

	/**
	 * 对应业务模型id，如果有则更新，否则新增
	 */
	private String[] instanceId;

	/**
	 * 发现模型id
	 */
	private String discoverId;

	/**
	 * 连接信息，利用json格式保存
	 */
	private String connect;

	/**
	 * 发送请求的序列号，用于标注是哪次请求
	 */
	private String seq = UUID.randomUUID().toString();

	/**
	 * 该字段用于保存已经实例化后的数据，直接用于保存数据库
	 */
	private String saveInstances;

	/**
	 * 该属性表明是由拓扑发现而来，需要存入实例化后的对象中
	 */
	private Long nodeId;

	/**
	 * 用于保存拓扑发现的接口实例信息
	 */
	private List<NtmNodeDto> ntmDto;

	/**
	 * 该属性表明是否为新版本实例化方式
	 */
	private boolean newVersion = false;

	/**
	 * 根结点的instId，对应线程发现
	 */
	private String rootInstId;

	/**
	 * 是否批量发现，默认否
	 */
	private boolean batchDiscover = false;

	/**
	 * 批量发现时导入的扩展属性
	 */
	private String extendProperties;

	public String getDiscoverId() {
		return discoverId;
	}

	public void setDiscoverId(String discoverId) {
		this.discoverId = discoverId;
	}

	public String getConnect() {
		return connect;
	}

	public void setConnect(String connect) {
		this.connect = connect;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String[] getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String[] instanceId) {
		this.instanceId = instanceId;
	}

	public String getSaveInstances() {
		return saveInstances;
	}

	public void setSaveInstances(String saveInstances) {
		this.saveInstances = saveInstances;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public List<NtmNodeDto> getNtmDto() {
		return ntmDto;
	}

	public void setNtmDto(List<NtmNodeDto> ntmDto) {
		this.ntmDto = ntmDto;
	}

	public boolean isNewVersion() {
		return newVersion;
	}

	public void setNewVersion(boolean newVersion) {
		this.newVersion = newVersion;
	}

	public String getRootInstId() {
		return rootInstId;
	}

	public void setRootInstId(String rootInstId) {
		this.rootInstId = rootInstId;
	}

	public boolean isBatchDiscover() {
		return batchDiscover;
	}

	public void setBatchDiscover(boolean batchDiscover) {
		this.batchDiscover = batchDiscover;
	}

	public String getExtendProperties() {
		return extendProperties;
	}

	public void setExtendProperties(String extendProperties) {
		this.extendProperties = extendProperties;
	}

}
