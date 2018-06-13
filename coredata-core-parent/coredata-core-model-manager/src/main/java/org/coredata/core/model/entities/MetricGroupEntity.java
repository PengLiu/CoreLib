package org.coredata.core.model.entities;


import org.coredata.core.model.common.MetricGroup;
import org.coredata.core.model.converter.MetricGroupConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 指标组模型表对应实体
 * @author sushi
 *
 */
@Entity
@Table(name = "t_metricgroup")
public class MetricGroupEntity extends BaseEntity {

	private static final long serialVersionUID = 1622923678060355532L;

	@Column(name = "metricgroup_model")
	@Convert(converter = MetricGroupConverter.class)
	private MetricGroup metricGroup;

	public MetricGroup getMetricGroup() {
		return metricGroup;
	}

	public void setMetricGroup(MetricGroup metricGroup) {
		this.metricGroup = metricGroup;
	}

}
