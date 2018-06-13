package org.coredata.core.model.api.dto;

import java.util.List;

/**
 * 数据传输的dto，用于保存实例化信息
 * @author sushiping
 *
 */
public class InstanceDto {

	public static final String ROOT_TYPE = "root";

	public static final String SINGLE_TYPE = "single";

	/**
	 * 实例id
	 */
	private String instId;

	/**
	 * 资源节点名称
	 */
	private String name;

	/**
	 * 资源节点显示名称
	 */
	private String displayName;

	/**
	 * 资源节点索引
	 */
	private String index;

	/**
	 * 资源类型，分为root，multiple，single
	 */
	private String nodeLevel;

	/**
	 * 资源所在层级
	 */
	private String level;

	/**
	 * 对应资源id，例如mysql，subdatabase
	 */
	private String resType;

	/**
	 * 对应模型id，例如：mysql，mysql_database
	 */
	private String modelId;

	/**
	 * 实例化对象对应的连接信息
	 */
	private String connections;

	/**
	 * 该层级下的子资源
	 */
	private List<InstanceDto> child;

	/**
	 * 相关联的属性集合
	 */
	private List<InstanceProperty> props;

	/**
	 * 对应资源的ip地址
	 */
	private String ip;

	/**
	 * 是否默认选中属性
	 */
	private String defaultSelected;

	/**
	 * 资产管理ip
	 */
	private String mainIp;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getConnections() {
		return connections;
	}

	public void setConnections(String connections) {
		this.connections = connections;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getInstId() {
		return instId;
	}

	public void setInstId(String instId) {
		this.instId = instId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getDefaultSelected() {
		return defaultSelected;
	}

	public void setDefaultSelected(String defaultSelected) {
		this.defaultSelected = defaultSelected;
	}

	public List<InstanceDto> getChild() {
		return child;
	}

	public void setChild(List<InstanceDto> child) {
		this.child = child;
	}

	public List<InstanceProperty> getProps() {
		return props;
	}

	public void setProps(List<InstanceProperty> props) {
		this.props = props;
	}

	public String getNodeLevel() {
		return nodeLevel;
	}

	public void setNodeLevel(String nodeLevel) {
		this.nodeLevel = nodeLevel;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getMainIp() {
		return mainIp;
	}

	public void setMainIp(String mainIp) {
		this.mainIp = mainIp;
	}

}
