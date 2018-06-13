package org.coredata.core.model.action.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Action implements Serializable {

	private static final long serialVersionUID = -1949977962503078233L;

	private String id;

	private String name;

	private String type;

	/**
	 * 所对应的instId
	 */
	private String sourceres;

	/**
	 * 所需对应metric指标名称
	 */
	private String[] sourcemetric;

	private String datatype;

	private String minvalue;

	private String maxvalue;

	private String steplength;

	private List<Controller> controller = new ArrayList<>();

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSourceres() {
		return sourceres;
	}

	public void setSourceres(String sourceres) {
		this.sourceres = sourceres;
	}

	public String[] getSourcemetric() {
		return sourcemetric;
	}

	public void setSourcemetric(String[] sourcemetric) {
		this.sourcemetric = sourcemetric;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getMinvalue() {
		return minvalue;
	}

	public void setMinvalue(String minvalue) {
		this.minvalue = minvalue;
	}

	public String getMaxvalue() {
		return maxvalue;
	}

	public void setMaxvalue(String maxvalue) {
		this.maxvalue = maxvalue;
	}

	public String getSteplength() {
		return steplength;
	}

	public void setSteplength(String steplength) {
		this.steplength = steplength;
	}

	public List<Controller> getController() {
		return controller;
	}

	public void setController(List<Controller> controller) {
		this.controller = controller;
	}

}
