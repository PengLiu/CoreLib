package org.coredata.core.model.service;

import com.alibaba.fastjson.JSON;
import com.rits.cloning.Cloner;
import org.coredata.core.model.entities.TransformEntity;
import org.coredata.core.model.repositories.TransformModelRepository;
import org.coredata.core.model.transform.Transform;
import org.coredata.core.model.transform.TransformModel;
import org.coredata.core.util.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
public class TransformService {

	@Autowired
	private TransformModelRepository transformModelRepository;


	@Autowired
	private RedisService redisService;

	private Cloner cloner = new Cloner();

	
	public TransformModel findById(String id) {
		TransformEntity transform = transformModelRepository.findById(id);
		if (transform == null)
			return null;
		return transform.getDecryptModel();
	}

	
	public void save(TransformModel model) {

		redisService.saveData(RedisService.TRANSFORM, model.getId(), model);
		String id = model.getId();
		TransformEntity transformEntity = transformModelRepository.findById(id);
		if (transformEntity != null)
			transformModelRepository.delete(transformEntity);
		transformEntity = new TransformEntity();
		transformEntity.setTransformModel(model);
		transformModelRepository.save(transformEntity);
	}

	
	public void deleteAll() {
		transformModelRepository.deleteAll();
	}



	
	public String findAllTransformModels() {
		Iterable<TransformEntity> transforms = transformModelRepository.findAll();
		List<TransformModel> models = new ArrayList<>();
		if (transforms == null)
			return JSON.toJSONString(models);
		transforms.forEach(transform -> models.add(transform.getTransformModel()));
		return JSON.toJSONString(models);
	}

	
	public String findTransformModelById(String modelId) {
		List<TransformModel> result = new ArrayList<>();
		TransformModel model = findById(modelId);
		if (model == null)
			return JSON.toJSONString(result);
		/**
		 * 此处暂时屏蔽原有逻辑，直接返回清洗模型
		 * List<Transform> transforms = new ArrayList<>();
		 * String origin = model.getOrigin();
		 * iteratorFindTransformModels(origin, transforms);
		 * model.getTransform().addAll(transforms);
		 */
		result.add(model);
		return JSON.toJSONString(result);
	}

	
	public long findAllTransformCount() {
		return transformModelRepository.count();
	}

	
	public void processSaveTransformModel(Map<String, TransformModel> models) {
		if (models.size() <= 0)
			return;
		//循环之后，再次存入数据库
		Set<TransformModel> results = new HashSet<>();
		models.forEach((k, model) -> {
			String origin = model.getOrigin();
			if (StringUtils.isEmpty(origin)) {//如果上级为空，直接存入数据库
				results.add(model);
				return;
			}
			TransformModel clone = cloner.deepClone(model);
			iteratorTransformModels(clone, origin, models, results);
		});
		results.forEach(r -> save(r));
	}

	/**
	 * 该方法用于循环迭代拼接清洗模型
	 * @param origin
	 * @param models
	 */
	private void iteratorTransformModels(TransformModel model, String origin, Map<String, TransformModel> models, Set<TransformModel> results) {
		TransformModel originModel = models.get(origin);
		if (originModel == null) {
			results.add(model);
			return;
		}
		List<Transform> originTransforms = originModel.getTransform();
		if (!CollectionUtils.isEmpty(originTransforms)) {
			List<Transform> cloneOriginTransforms = cloner.deepClone(originTransforms);
			List<Transform> transform = model.getTransform();
			if (transform == null) {
				transform = new ArrayList<>();
				model.setTransform(transform);
			}
			model.getTransform().addAll(cloneOriginTransforms);
		}
		String newOrigin = originModel.getOrigin();
		if (StringUtils.isEmpty(newOrigin)) {
			results.add(model);
			return;
		}
		iteratorTransformModels(model, newOrigin, models, results);
	}

	
	public List<TransformModel> findTransformModelsByOrigin(String origin) {
		List<TransformModel> models = new ArrayList<>();
		List<TransformEntity> transforms = transformModelRepository.findByOrigin(origin);
		if (CollectionUtils.isEmpty(transforms))
			return models;
		transforms.forEach(transform -> models.add(transform.getTransformModel()));
		return models;
	}

	
	public void delete(String id) {
		TransformEntity transformModel = transformModelRepository.findById(id);
		if (transformModel != null)
			transformModelRepository.delete(transformModel);
	}

	/**
	 * 该方法用于循环迭代获取全部采集模型
	private void iteratorFindTransformModels(String modelId, List<Transform> transforms) {
		if (!StringUtils.isEmpty(modelId)) {
			TransformModel originModel = findById(modelId);
			if (originModel != null) {
				transforms.addAll(originModel.getTransform());
				String origin = originModel.getOrigin();
				iteratorFindTransformModels(origin, transforms);
			}
		}
	}
	*/

}
