package org.coredata.core.entities.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.entities.CommEntity;
import org.coredata.core.entities.repositories.EntityResp;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.neo4j.graphdb.Direction;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Transactional(readOnly = true)
public class EntityService {

	private Logger logger = LoggerFactory.getLogger(EntityService.class);

	@Autowired
	private EntityResp entityResp;

	@Autowired
	private Session session;

	private BlockingQueue<CommEntity> entityCache = new LinkedBlockingQueue<>();

	private AtomicLong counter = new AtomicLong();

	@Scheduled(fixedDelay = 1000)
	public void save() {
		List<CommEntity> tmp = new ArrayList<>();
		entityCache.drainTo(tmp);
		if (!CollectionUtils.isEmpty(tmp)) {
			List<CommEntity> batch = new ArrayList<>();
			for (CommEntity entity : tmp) {
				CommEntity dbEntity = findByTokenAndEntityId(entity.getToken(), entity.getEntityId());
				if (dbEntity != null) {
					entity.setId(dbEntity.getId());
					batch.add(entity);
				} else {
					batch.add(entity);
				}
			}
			save(batch);
		}

	}

	@Scheduled(fixedDelay = 1000)
	public void logger() {
		System.err.println("Processed " + counter.getAndSet(0) + " records/second.");
	}

	@Async
	public void saveAsync(CommEntity... entities) {
		for (CommEntity entity : entities) {
			entityCache.add(entity);
			counter.incrementAndGet();
		}
	}

	@Transactional
	public void removeAllEntities() {
		entityResp.deleteAll();
	}

	public CommEntity findById(Long id) {
		return entityResp.findById(id).get();
	}

	public CommEntity findByTokenAndEntityId(String token, String entityId) {
		return entityResp.findByTokenAndEntityId(token, entityId);
	}

	public Long countEntityByCondition(String queryJson) throws QuerydslException {

		StringBuilder cypherBuilder = new StringBuilder();
		cypherBuilder.append("MATCH (e:CommEntity) ");

		if (!StringUtils.isEmpty(queryJson)) {
			Neo4jFilterBuilder builder = new Neo4jFilterBuilder(queryJson);

			String filter = builder.buildFilters();
			if (!StringUtils.isEmpty(filter)) {
				cypherBuilder.append(" WHERE ").append(filter);
			}
		}

		String counter = cypherBuilder.toString() + " RETURN count(e)";

		Map<String, String> params = new HashMap<>();
		long total = session.queryForObject(Long.class, counter, params);
		return total;
	}

	public Map<Object, Long> countEntityByPropAndCondition(String prop, String queryJson) throws QuerydslException {

		Map<Object, Long> result = new HashMap<>();

		Neo4jFilterBuilder builder = new Neo4jFilterBuilder(queryJson);

		StringBuilder cypherBuilder = new StringBuilder();
		cypherBuilder.append("MATCH (e:CommEntity) ");

		String filter = builder.buildFilters();
		if (!StringUtils.isEmpty(filter)) {
			cypherBuilder.append(" WHERE ").append(filter);
		}

		if (prop.startsWith("props.") || prop.startsWith("conn.")) {
			prop = "e['" + prop + "']";
		} else {
			prop = "e." + prop;
		}

		cypherBuilder.append(" RETURN ").append(prop).append(" as key ").append(", COUNT(").append(prop).append(") AS size");

		Map<String, String> params = new HashMap<>();

		Result dbResult = session.query(cypherBuilder.toString(), params);
		dbResult.forEach(r -> {
			result.put(r.get("key"), (Long) r.get("size"));
		});
		return result;
	}

	public Page<CommEntity> findEntitiesByCondition(String queryJson) throws QuerydslException {

		Neo4jFilterBuilder builder = new Neo4jFilterBuilder(queryJson);

		StringBuilder cypherBuilder = new StringBuilder();
		cypherBuilder.append("MATCH (e:CommEntity) ");

		String filter = builder.buildFilters();
		if (!StringUtils.isEmpty(filter)) {
			cypherBuilder.append(" WHERE ").append(filter);
		}

		String counter = cypherBuilder.toString() + " RETURN count(e)";
		cypherBuilder.append(" RETURN e ").append(builder.orderBy()).append(builder.pagination());

		if (logger.isDebugEnabled()) {
			logger.debug(counter);
			logger.debug(cypherBuilder.toString());
		}

		Map<String, String> params = new HashMap<>();
		long total = session.queryForObject(Long.class, counter, params);
		List<CommEntity> content = new ArrayList<>();
		if (total > 0) {
			Iterable<CommEntity> entities = session.query(CommEntity.class, cypherBuilder.toString(), params);
			entities.forEach(entity -> {
				content.add(entity);
			});
		}

		Page<CommEntity> result = new PageImpl<CommEntity>(content, builder.pageable(), total);
		return result;
	}

	public CommEntity findByEntityId(@Param("entityId") String entityId) {
		Page<CommEntity> page = entityResp.findByEntityId(entityId, PageRequest.of(1, 1));
		if (page.getTotalElements() > 0) {
			return page.getContent().get(0);
		}
		return new CommEntity();
	}

	public Page<CommEntity> findByEntityId(@Param("entityId") String entityId, Pageable pageable) {
		return entityResp.findByEntityId(entityId, pageable);
	}

	@Transactional
	public <S extends CommEntity> Iterable<S> save(List<S> entities) {
		return entityResp.saveAll(entities);
	}

	@Transactional
	public CommEntity save(CommEntity entity) {
		return entityResp.save(entity);
	}

	@Transactional
	public void delete(List<Long> ids) {
		for (Long id : ids) {
			entityResp.deleteById(id);
		}
	}

	@Transactional
	public void createRelationship(String srcEntityId, String destEntityId, String relation, Direction direction) {
		StringBuilder builder = new StringBuilder();
		builder.append("MATCH (a:CommEntity),(b:CommEntity) WHERE a.entityId = {srcEntityId} AND b.entityId = {destEntityId} CREATE (a)");
		if (direction == Direction.INCOMING) {
			builder.append("<-[r:").append(relation).append("]-(b)");
		} else if (direction == Direction.OUTGOING) {
			builder.append("-[r:").append(relation).append("]->(b)");
		} else {
			builder.append("-[r:").append(relation).append("]-(b)");
		}
		Map<String, String> param = new HashMap<>();
		param.put("srcEntityId", srcEntityId);
		param.put("destEntityId", destEntityId);
		session.query(builder.toString(), param);
	}

	@Transactional
	public void createRelationship(long fromId, long toId, String relation, Direction direction) {
		StringBuilder builder = new StringBuilder();
		builder.append("MATCH (a:CommEntity),(b:CommEntity) WHERE ID(a) = {fromId} AND ID(b) = {toId} CREATE (a)");
		if (direction == Direction.INCOMING) {
			builder.append("<-[r:").append(relation).append("]-(b)");
		} else if (direction == Direction.OUTGOING) {
			builder.append("-[r:").append(relation).append("]->(b)");
		} else {
			builder.append("-[r:").append(relation).append("]-(b)");
		}

		Map<String, Long> param = new HashMap<>();
		param.put("fromId", fromId);
		param.put("toId", toId);
		session.query(builder.toString(), param);
	}

	@Transactional
	public void createRelationship(long fromId, long[] toId, String relation, Direction direction) {
		for (int i = 0; i < toId.length; i++) {
			StringBuilder builder = new StringBuilder();
			builder.append("MATCH (a:CommEntity),(b:CommEntity) WHERE ID(a) = {fromId} AND ID(b) = {toId} CREATE (a)");
			if (direction == Direction.INCOMING) {
				builder.append("<-[r:").append(relation).append("]-(b)");
			} else if (direction == Direction.OUTGOING) {
				builder.append("-[r:").append(relation).append("]->(b)");
			} else {
				builder.append("-[r:").append(relation).append("]-(b)");
			}
			Map<String, Long> param = new HashMap<>();
			param.put("fromId", fromId);
			param.put("toId", toId[i]);
			session.query(builder.toString(), param);
		}
	}

	public Collection<CommEntity> findByRelatedEntity(long fromEntity, String relationship, int depth, Direction direction) {

		Collection<CommEntity> entities = new ArrayList<>();

		StringBuilder builder = new StringBuilder();
		builder.append("MATCH (a:CommEntity)");
		if (direction == Direction.INCOMING) {
			builder.append("<-[");
		} else {
			builder.append("-[");
		}
		builder.append("r:").append(relationship);
		if (depth > 0) {
			builder.append("*..").append(depth);
		}
		if (direction == Direction.INCOMING) {
			builder.append("]-");
		} else {
			builder.append("]->");
		}
		builder.append("(b:CommEntity) WHERE ID(a) = {fromEntity} RETURN b");

		Map<String, Long> param = new HashMap<>();
		param.put("fromEntity", fromEntity);
		Result result = session.query(builder.toString(), param);
		result.forEach(item -> {
			item.values().forEach(entity -> {
				entities.add((CommEntity) entity);
			});
		});
		return entities;
	}

}
