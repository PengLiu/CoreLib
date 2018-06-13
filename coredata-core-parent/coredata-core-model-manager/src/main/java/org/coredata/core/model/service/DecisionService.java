package org.coredata.core.model.service;

import com.alibaba.fastjson.JSON;
import com.rits.cloning.Cloner;
import org.coredata.core.model.decision.*;
import org.coredata.core.model.entities.DecisionEntity;
import org.coredata.core.model.repositories.DecisionModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
public class DecisionService {

	private static final String ACTION_TYPE = "stateTransition";

	@Autowired
	private DecisionModelRepository decisionModelRepository;

	private Cloner cloner = new Cloner();

	
	public void save(DecisionModel model) {
		String id = model.getId();
		DecisionEntity decisionEntity = decisionModelRepository.findById(id);
		if (decisionEntity != null)
			decisionModelRepository.delete(decisionEntity);
		decisionEntity = new DecisionEntity();
		decisionEntity.setDecisionModel(model);
		decisionModelRepository.save(decisionEntity);
	}

	
	public void delete(String id) {
		DecisionEntity decision = decisionModelRepository.findById(id);
		if (decision != null)
			decisionModelRepository.delete(decision);
	}

	
	public String findAllDecisionModels() {

		Iterable<DecisionEntity> decisions = decisionModelRepository.findAll();
		List<DecisionModel> models = new ArrayList<>();
		if (decisions == null)
			return JSON.toJSONString(models);
		decisions.forEach(decision -> models.add(decision.getDecisionModel()));
		return JSON.toJSONString(models);
	}

	
	public String findDecisionModelById(String modelId) {
		List<DecisionModel> result = new ArrayList<>();
		DecisionEntity decision = decisionModelRepository.findById(modelId);
		if (decision != null && decision.getDecisionModel() != null) {
			result.add(decision.getDecisionModel());
		}
		return JSON.toJSONString(result);
	}

	
	public void saveDecisionModel(DecisionModel model) {
		List<Decision> decision = model.getDecision();
		if (decision != null) {
			for (Decision d : decision) {
				if (d.getEnable() != null)
					continue;
				boolean enable = false;
				List<DecisionRule> rule = d.getRule();
				if (rule == null)
					continue;
				for (DecisionRule r : rule) {
					List<Action> action = r.getAction();
					if (action == null)
						continue;
					for (Action a : action) {
						Flapping flapping = a.getFlapping();
						if (flapping == null) {
							flapping = new Flapping();
							a.setFlapping(flapping);
						}
						if (ACTION_TYPE.equals(a.getType()))
							enable = true;
					}
				}
				d.setEnable(enable);
			}
		}
		save(model);
	}

	
	public DecisionModel findDecisionModelEntityById(String modelId) {
		DecisionEntity decision = decisionModelRepository.findById(modelId);
		if (decision == null)
			return null;
		DecisionModel model = decision.getDecisionModel();
		return model;
	}

	
	public long findAllDecisionCount() {
		return decisionModelRepository.count();
	}

	
	public void processSaveDecisionModel(Map<String, DecisionModel> models) {
		if (models.size() <= 0)
			return;
		//循环之后，再次存入数据库
		Set<DecisionModel> results = new HashSet<>();
		models.forEach((k, model) -> {
			String origin = model.getOrigin();
			if (StringUtils.isEmpty(origin)) {//如果上级为空，直接存入数据库
				results.add(model);
				return;
			}
			DecisionModel clone = cloner.deepClone(model);
			iteratorDecisionModels(clone, origin, models, results);
		});
		results.forEach(r -> saveDecisionModel(r));
	}

	/**
	 * 该方法用于循环迭代拼接决策模型
	 * @param origin
	 * @param models
	 */
	private void iteratorDecisionModels(DecisionModel model, String origin, Map<String, DecisionModel> models, Set<DecisionModel> results) {
		DecisionModel originModel = models.get(origin);
		if (originModel == null) {
			results.add(model);
			return;
		}
		List<Decision> originDecision = originModel.getDecision();
		if (!CollectionUtils.isEmpty(originDecision)) {
			List<Decision> cloneOriginDecision = cloner.deepClone(originDecision);
			List<Decision> decision = model.getDecision();
			if (decision == null) {
				decision = new ArrayList<>();
				model.setDecision(decision);
			}
			model.getDecision().addAll(cloneOriginDecision);
		}
		String newOrigin = originModel.getOrigin();
		if (StringUtils.isEmpty(newOrigin)) {
			results.add(model);
			return;
		}
		iteratorDecisionModels(model, newOrigin, models, results);
	}

	public List<DecisionModel> findDecisionModelsByOrigin(String origin) {
		List<DecisionEntity> decisions = decisionModelRepository.findByOrigin(origin);
		List<DecisionModel> models = new ArrayList<>();
		if (CollectionUtils.isEmpty(decisions))
			return models;
		decisions.forEach(decision -> models.add(decision.getDecisionModel()));
		return models;
	}

}
