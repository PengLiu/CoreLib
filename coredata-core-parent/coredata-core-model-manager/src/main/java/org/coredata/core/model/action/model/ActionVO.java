package org.coredata.core.model.action.model;

import java.util.ArrayList;
import java.util.List;

public class ActionVO {

	private String id;

	private String name;
	
	/**
	 * 所需对应metric指标名称
	 */
	private String[] sourcemetric;

	private String datatype;

	private List<ControllerVO> controller = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the controller
	 */
	public List<ControllerVO> getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(List<ControllerVO> controller) {
		this.controller = controller;
	}

	/**
	 * @return the sourcemetric
	 */
	public String[] getSourcemetric() {
		return sourcemetric;
	}

	/**
	 * @param sourcemetric the sourcemetric to set
	 */
	public void setSourcemetric(String[] sourcemetric) {
		this.sourcemetric = sourcemetric;
	}

	/**
	 * @return the datatype
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

}
