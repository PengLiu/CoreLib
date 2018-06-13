package org.coredata.core.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.model.common.Metric;
import org.coredata.core.model.common.MetricGroup;
import org.coredata.core.model.entities.MetricEntity;
import org.coredata.core.model.entities.MetricGroupEntity;
import org.coredata.core.model.repositories.MetricGroupRepository;
import org.coredata.core.model.repositories.MetricRepository;
import org.coredata.core.util.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MetricModelService {

	@Autowired
	private MetricGroupRepository metricGroupRepository;

	@Autowired
	private MetricRepository metricRepository;

	@Autowired
	private RedisService redisService;

	public void saveMetricGroup(MetricGroup group) {
		if (group == null)
			return;
		String id = group.getId();
		MetricGroupEntity metricGroupEntity = metricGroupRepository.findById(id);
		if (metricGroupEntity != null)
			metricGroupRepository.delete(metricGroupEntity);
		metricGroupEntity = new MetricGroupEntity();
		metricGroupEntity.setMetricGroup(group);
		metricGroupRepository.save(metricGroupEntity);
	}

	public void saveMetric(Metric metric) {
		if (metric == null)
			return;
		String id = metric.getId();
		MetricEntity metricEntity = metricRepository.findById(id);
		if (metricEntity != null)
			metricRepository.delete(metricEntity);
		metricEntity = new MetricEntity();
		metricEntity.setMetricModel(metric);
		metricRepository.save(metricEntity);
		saveRedisMetric(metric);
	}

	public Metric findById(String id) {
		MetricEntity metricEntity = metricRepository.findById(id);
		if (metricEntity == null)
			return null;
		return metricEntity.getMetricModel();
	}

	public Map<String, Metric> findNamesByIds(String[] ids) {
		Map<String, Metric> result = new HashMap<>();
		for (String id : ids) {
			Metric metric = findById(id);
			result.put(id, metric);
		}
		return result;
	}

	public long findAllMetricCount() {
		return metricRepository.count();
	}

	public long findAllMetricGroupCount() {
		return metricGroupRepository.count();
	}

	private void saveRedisMetric(Metric metric) {
		redisService.saveData(RedisService.METRIC, metric.getId(), metric);
	}

	public void initAllMetricsToRedis() {
		Iterable<MetricEntity> metrics = metricRepository.findAll();
		for (MetricEntity metric : metrics) {
			Metric model = metric.getMetricModel();
			saveRedisMetric(model);
		}
	}

	public List<Metric> findAllMetrics() {
		List<Metric> metrics = new ArrayList<>();
		Iterable<MetricEntity> ms = metricRepository.findAll();
		for (MetricEntity metric : ms) {
			Metric m = metric.getMetricModel();
			metrics.add(m);
		}
		return metrics;
	}

}
