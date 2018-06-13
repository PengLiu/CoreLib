package org.coredata.core.model.common;

import java.io.Serializable;

public class Metric implements Serializable {

	private static final long serialVersionUID = 4933781470085307989L;

	private String id;

	private String name;

	private String desc;

	private String metrictype;

	private String unit;

	private String datatype;

	private String seriesid;

	private String groupid;

	public static final String AVAIL_METRIC = "avail";

	/**
	 * 是否系统默认模型，默认是
	 */
	private int isSystem = 1;

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getMetrictype() {
		return metrictype;
	}

	public void setMetrictype(String metrictype) {
		this.metrictype = metrictype;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getSeriesid() {
		return seriesid;
	}

	public void setSeriesid(String seriesid) {
		this.seriesid = seriesid;
	}

	public String getGroupid() {
		return groupid;
	}

	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	public static enum MetricDataType {
		Percentage, Speed, Count, Capacity, Cumulative, State, Control, Temperature, Time, Flow, Frequency, String, Table, Picture;
	}

	/**
	 * 该方法用于判定是否需要四舍五入
	 * @param datatype
	 * @return
	 */
	public boolean needScale(MetricDataType datatype) {
		return !(MetricDataType.State.equals(datatype) || MetricDataType.Control.equals(datatype) || MetricDataType.Temperature.equals(datatype)
				|| MetricDataType.Time.equals(datatype) || MetricDataType.String.equals(datatype) || MetricDataType.Table.equals(datatype)
				|| MetricDataType.Picture.equals(datatype));
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
