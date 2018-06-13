package org.coredata.core.model.action.model;

public class ControllerVO {

	private String id;

	private String name;
		
	private String metricvalue;

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
	 * @return the metricvalue
	 */
	public String getMetricvalue() {
		return metricvalue;
	}

	/**
	 * @param metricvalue the metricvalue to set
	 */
	public void setMetricvalue(String metricvalue) {
		this.metricvalue = metricvalue;
	}

}
