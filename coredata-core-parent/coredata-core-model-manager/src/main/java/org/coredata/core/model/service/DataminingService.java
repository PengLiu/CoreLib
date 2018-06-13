package org.coredata.core.model.service;

import com.alibaba.fastjson.JSON;
import com.rits.cloning.Cloner;
import org.coredata.core.model.common.Metric;
import org.coredata.core.model.entities.MiningEntity;
import org.coredata.core.model.mining.Datamining;
import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.model.mining.Expression;
import org.coredata.core.model.mining.Type;
import org.coredata.core.model.repositories.DataminingModelRepository;
import org.coredata.core.util.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class DataminingService {

	private static final String METRIC_ID = "id";

	private static final String METRIC_NAME = "name";

	private static final String METRIC_TYPE = "type";

	@Autowired
	private DataminingModelRepository dataminingModelRepository;

	@Autowired
	private RedisService redisService;

	@Autowired
	private MetricModelService metricModelService;

	private Cloner cloner = new Cloner();

	public void save(DataminingModel model) {
		redisService.saveData(RedisService.MINING, model.getId(), model);
		String id = model.getId();
		MiningEntity miningEntity = dataminingModelRepository.findById(id);
		if (miningEntity != null)
			dataminingModelRepository.delete(miningEntity);
		miningEntity = new MiningEntity();
		miningEntity.setMiningModel(model);
		dataminingModelRepository.save(miningEntity);
	}

	public void deleteAll() {
		dataminingModelRepository.deleteAll();
	}

	public void delete(String id) {
		MiningEntity datamining = dataminingModelRepository.findById(id);
		if (datamining == null)
			return;
		dataminingModelRepository.delete(datamining);
	}

	public DataminingModel findById(String id) {
		MiningEntity datamining = dataminingModelRepository.findById(id);
		if (datamining == null) {
			return null;
		}
		return datamining.getDecryptModel();
	}

	public String findAllDataminingModels() {
		Iterable<MiningEntity> dataminings = dataminingModelRepository.findAll();
		List<DataminingModel> models = new ArrayList<>();
		if (dataminings == null) {
			return JSON.toJSONString(models);
		}
		dataminings.forEach(mining -> models.add(mining.getMiningModel()));
		return JSON.toJSONString(models);
	}

	public String findDataminingModelById(String modelId) {
		List<DataminingModel> result = new ArrayList<>();
		DataminingModel model = findById(modelId);
		if (model == null) {
			return JSON.toJSONString(result);
		}
		/**
		 * 此处暂时屏蔽原有逻辑，直接返回清洗模型
		 * List<Datamining> minings = new ArrayList<>();
		 * String origin = model.getOrigin();
		 * iteratorFindDataminingModels(origin, minings);
		 * model.getMining().addAll(minings);
		 */
		result.add(model);
		return JSON.toJSONString(result);
	}

	public long findAllDataminingCount() {
		return dataminingModelRepository.count();
	}

	public void processSaveDataminingModel(Map<String, DataminingModel> models) {
		if (models.size() <= 0) {
			return;
		}
		//循环之后，再次存入数据库
		Set<DataminingModel> results = new HashSet<>();
		models.forEach((k, model) -> {
			String origin = model.getOrigin();
			if (StringUtils.isEmpty(origin)) {//如果上级为空，直接存入数据库
				results.add(model);
				return;
			}
			DataminingModel clone = cloner.deepClone(model);
			iteratorDataminingModels(clone, origin, models, results);
		});
		results.forEach(r -> save(r));
	}

	/**
	 * 该方法用于循环迭代拼接挖掘模型
	 *
	 * @param origin
	 * @param models
	 */
	private void iteratorDataminingModels(DataminingModel model, String origin, Map<String, DataminingModel> models, Set<DataminingModel> results) {
		DataminingModel originModel = models.get(origin);
		if (originModel == null) {
			results.add(model);
			return;
		}
		List<Datamining> originDatamining = originModel.getMining();
		if (!CollectionUtils.isEmpty(originDatamining)) {
			List<Datamining> cloneOriginDatamining = cloner.deepClone(originDatamining);
			List<Datamining> datamining = model.getMining();
			if (datamining == null) {
				datamining = new ArrayList<>();
				model.setMining(datamining);
			}
			model.getMining().addAll(cloneOriginDatamining);
		}
		String newOrigin = originModel.getOrigin();
		if (StringUtils.isEmpty(newOrigin)) {
			results.add(model);
			return;
		}
		iteratorDataminingModels(model, newOrigin, models, results);
	}

	public List<DataminingModel> findDataminingModelsByOrigin(String origin) {
		List<DataminingModel> models = new ArrayList<>();
		List<MiningEntity> minings = dataminingModelRepository.findByOrigin(origin);
		if (CollectionUtils.isEmpty(minings)) {
			return models;
		}
		minings.forEach(mining -> models.add(mining.getMiningModel()));
		return models;
	}


	public List<Map<String, String>> findAllMetricInfo(String modelId) {
		List<Map<String, String>> result = new ArrayList<>();
		DataminingModel datamining = findById(modelId);
		if (datamining == null)
			return result;
		List<Datamining> minings = datamining.getMining();
		if (CollectionUtils.isEmpty(minings))
			return result;
		for (Datamining m : minings) {
			Type type = m.getType();
			if (type == null)
				continue;
			List<Expression> exps = type.getExp();
			if (exps == null || exps.size() <= 0)
				continue;
			for (Expression exp : exps) {
				String mt = exp.getMetric();
				String eid = mt.split(":")[0];
				// 根据id获取一下对应metric信息，获取对应名称
				Metric metric = metricModelService.findById(eid);
				if (metric == null)
					continue;
				Map<String, String> r = new HashMap<>();
				r.put(METRIC_ID, eid);
				r.put(METRIC_NAME, metric.getName());
				r.put(METRIC_TYPE, metric.getMetrictype());
				result.add(r);
			}
		}
		return result;
	}

}
