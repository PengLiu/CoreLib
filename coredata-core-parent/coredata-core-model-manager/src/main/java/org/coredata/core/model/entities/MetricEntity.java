package org.coredata.core.model.entities;


import org.coredata.core.model.common.Metric;
import org.coredata.core.model.converter.MetricConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 指标模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_metric")
public class MetricEntity extends BaseEntity {

	private static final long serialVersionUID = -3238211085254287529L;

	@Column(name = "metric_model")
	@Convert(converter = MetricConverter.class)
	private Metric metricModel;

	public Metric getMetricModel() {
		return metricModel;
	}

	public void setMetricModel(Metric metricModel) {
		this.metricModel = metricModel;
	}

}
