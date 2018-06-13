package org.coredata.core.agent.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.coredata.core.agent.entities.Agent;
import org.coredata.core.agent.entities.AgentTask;
import org.coredata.core.agent.repositories.AgentResp;
import org.coredata.core.agent.repositories.AgentTaskResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AgentService {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private AgentResp agentResp;

	@Autowired
	private AgentTaskResp agentTaskResp;

	@Transactional
	public Agent saveAgent(Agent agent) {
		return agentResp.save(agent);
	}

	@Transactional
	public void saveAgent(List<Agent> agents) {
		agentResp.saveAll(agents);
	}

	public Page<Agent> findAllAgents(Pageable pageable) {
		return agentResp.findAll(pageable);
	}

	public Page<Agent> findByCondition(Map<String, Object> condition, Pageable pageable) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Agent> query = builder.createQuery(Agent.class);

		Root<Agent> root = query.from(Agent.class);

		List<Predicate> predicates = new ArrayList<Predicate>();
		for (Entry<String, Object> entry : condition.entrySet()) {
			Predicate predicate = builder.equal(root.get(entry.getKey()), entry.getValue());
			predicates.add(predicate);
		}
		query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

		Iterator<Sort.Order> ite = pageable.getSort().iterator();
		List<Order> orders = new ArrayList<>();
		while (ite.hasNext()) {
			Sort.Order tmp = ite.next();
			orders.add(tmp.getDirection() == Direction.ASC ? builder.asc(root.get(tmp.getProperty())) : builder.desc(root.get(tmp.getProperty())));
		}
		query.orderBy(orders);

		TypedQuery<Agent> createQuery = entityManager.createQuery(query);
		Integer pageSize = pageable.getPageSize();
		Integer pageNo = pageable.getPageNumber();

		TypedQuery<Agent> createCountQuery = entityManager.createQuery(query);
		int startIndex = pageSize * pageNo;
		createQuery.setFirstResult(startIndex);
		createQuery.setMaxResults(pageable.getPageSize());
		Page<Agent> pageRst = new PageImpl<Agent>(createQuery.getResultList(), pageable, createCountQuery.getResultList().size());
		return pageRst;
	}

	public long findCountByCondition(Map<String, Object> condition) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Agent> root = query.from(Agent.class);
		List<Predicate> predicates = new ArrayList<Predicate>();
		for (Entry<String, Object> entry : condition.entrySet()) {
			Predicate predicate = builder.equal(root.get(entry.getKey()), entry.getValue());
			predicates.add(predicate);
		}
		query.select(builder.count(root));
		query.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		return entityManager.createQuery(query).getSingleResult();
	}

	public Agent findById(long id) {
		Optional<Agent> opt = agentResp.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		} else {
			return null;
		}
	}

	@Transactional
	public void removeById(long id) {
		agentResp.deleteById(id);
	}

	@Transactional
	public void removeByIp(String ipAddr) {
		agentResp.removeByIpAddress(ipAddr);
	}

	public long findAgentsCount() {
		return agentResp.count();
	}

	public Page<AgentTask> findTaskByEntity(String entityId, Pageable pageable) {
		return agentTaskResp.findByEntityId(entityId, pageable);
	}

	public Page<Agent> findByToken(String token, Pageable pageable) {
		return agentResp.findByToken(token, pageable);
	}

	public Page<Agent> findByFeatureAndStatus(String feature, int status, Pageable pageable) {
		return agentResp.findByFeatures(feature, status, pageable);
	}

	/**
	 * TODO 王伟正在进行Agent下发改造
	 * 该方法用于向Agent下发相关命令
	 * @return
	 * @throws Exception
	 */
	public String sendAgentCmd(String cmd, String... agentIps) throws Exception {
		String result = "";

		return result;
	}

}