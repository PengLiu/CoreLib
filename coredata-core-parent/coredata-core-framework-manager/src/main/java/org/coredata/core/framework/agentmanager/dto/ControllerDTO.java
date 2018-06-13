package org.coredata.core.framework.agentmanager.dto;

import java.util.ArrayList;
import java.util.List;

public class ControllerDTO {

	private String id;

	private String name;

	private String cmd;

	private String protocol;
	
	private String instanceId;
	
	private String modelId;
	
	private String collectType;

	
	private List<Param> param = new ArrayList<>();

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the cmd
	 */
	public String getCmd() {
		return cmd;
	}

	/**
	 * @param cmd the cmd to set
	 */
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the param
	 */
	public List<Param> getParam() {
		return param;
	}

	/**
	 * @param param the param to set
	 */
	public void setParam(List<Param> param) {
		this.param = param;
	}

	/**
	 * @return the instenceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId the instenceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the modelId
	 */
	public String getModelId() {
		return modelId;
	}

	/**
	 * @param modelId the modelId to set
	 */
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	/**
	 * @return the collectType
	 */
	public String getCollectType() {
		return collectType;
	}

	/**
	 * @param collectType the collectType to set
	 */
	public void setCollectType(String collectType) {
		this.collectType = collectType;
	}

}
