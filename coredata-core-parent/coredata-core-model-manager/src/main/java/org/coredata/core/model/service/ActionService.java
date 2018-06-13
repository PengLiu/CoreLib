package org.coredata.core.model.service;

import com.rits.cloning.Cloner;
import org.coredata.core.model.action.model.Action;
import org.coredata.core.model.action.model.ActionModel;
import org.coredata.core.model.entities.ActionEntity;
import org.coredata.core.model.repositories.ActionModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;


@Service
@Transactional
public class ActionService  {

	@Autowired
	private ActionModelRepository actionModelRepository;

	private Cloner cloner = new Cloner();

	/**
	 * 保存动作模型，先按照模型id获取是否已经存在该模型
	 */
	
	public void save(ActionModel model) {
		if (model == null)
			return;
		String id = model.getId();
		ActionEntity actionEntity = actionModelRepository.findById(id);
		if (actionEntity != null)
			actionModelRepository.delete(actionEntity);
		actionEntity = new ActionEntity();
		actionEntity.setActionModel(model);
		actionModelRepository.save(actionEntity);
	}

	
	public void deleteAll() {
		actionModelRepository.deleteAll();
	}

	
	public ActionModel findActionModelByModelId(String modelId) {
		ActionEntity actionEntity = actionModelRepository.findById(modelId);
		if (actionEntity == null)
			return null;
		return actionEntity.getActionModel();
	}

	
	public long findAllActionCount() {
		return actionModelRepository.count();
	}

	
	public void processSaveActionModel(Map<String, ActionModel> models) {
		if (models.size() <= 0)
			return;
		//循环之后，再次存入数据库
		Set<ActionModel> results = new HashSet<>();
		models.forEach((k, model) -> {
			String origin = model.getOrigin();
			if (StringUtils.isEmpty(origin)) {//如果上级为空，直接存入数据库
				results.add(model);
				return;
			}
			ActionModel clone = cloner.deepClone(model);
			iteratorActionModels(clone, origin, models, results);
		});
		results.forEach(r -> save(r));
	}

	/**
	 * 该方法用于循环迭代拼接决策模型
	 * @param origin
	 * @param models
	 */
	private void iteratorActionModels(ActionModel model, String origin, Map<String, ActionModel> models, Set<ActionModel> results) {
		ActionModel originModel = models.get(origin);
		if (originModel == null) {
			results.add(model);
			return;
		}
		List<Action> originAction = originModel.getAction();
		if (!CollectionUtils.isEmpty(originAction)) {
			List<Action> cloneOriginAction = cloner.deepClone(originAction);
			List<Action> action = model.getAction();
			if (action == null) {
				action = new ArrayList<>();
				model.setAction(action);
			}
			model.getAction().addAll(cloneOriginAction);
		}
		String newOrigin = originModel.getOrigin();
		if (StringUtils.isEmpty(newOrigin)) {
			results.add(model);
			return;
		}
		iteratorActionModels(model, newOrigin, models, results);
	}

	
	public List<ActionModel> findActionModelsByOrigin(String origin) {
		List<ActionModel> models = new ArrayList<>();
		List<ActionEntity> entitys = actionModelRepository.findByOrigin(origin);
		if (CollectionUtils.isEmpty(entitys))
			return models;
		entitys.forEach(entity -> models.add(entity.getActionModel()));
		return models;
	}

	
	public void delete(String id) {
		ActionEntity entity = actionModelRepository.findById(id);
		if (entity == null)
			return;
		actionModelRepository.delete(entity);
	}
}
