package org.coredata.core.model.common;

import java.util.ArrayList;
import java.util.List;

public class MetricGroups {

	private List<MetricGroup> metricGroup = new ArrayList<MetricGroup>();

	private List<Metric> metric = new ArrayList<>();

	public List<MetricGroup> getMetricGroup() {
		return metricGroup;
	}

	public void setMetricGroup(List<MetricGroup> metricGroup) {
		this.metricGroup = metricGroup;
	}

	public List<Metric> getMetric() {
		return metric;
	}

	public void setMetric(List<Metric> metric) {
		this.metric = metric;
	}

}
