package org.coredata.core.entities.repositories;

import org.coredata.core.entities.CommEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

public interface EntityResp extends Neo4jRepository<CommEntity, Long> {

	@Query(value = "MATCH (e) WHERE e.entityId = {entityId} RETURN e ORDER BY e.createdTime DESC", countQuery = "MATCH (e) WHERE e.entityId = {entityId} RETURN COUNT(e)")
	Page<CommEntity> findByEntityId(@Param("entityId") String entityId, Pageable pageable);

	@Query(value = "MATCH (e) WHERE e.token = {token} RETURN e ORDER BY e.createdTime DESC", countQuery = "MATCH (e) WHERE e.token = {token} RETURN COUNT(e)")
	Page<CommEntity> findByToken(@Param("token") String token, Pageable pageable);

	@Query(value = "MATCH (e) WHERE e.token = {token} AND e.entityId = {entityId} RETURN e")
	CommEntity findByTokenAndEntityId(@Param("token") String token, @Param("entityId") String entityId);
}