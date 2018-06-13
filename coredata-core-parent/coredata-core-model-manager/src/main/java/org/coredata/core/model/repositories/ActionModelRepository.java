package org.coredata.core.model.repositories;

import org.coredata.core.model.entities.ActionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ActionModelRepository extends CrudRepository<ActionEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_action a WHERE a.action_id = ?1 ")
	ActionEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_action a WHERE a.action_origin = ?1 ")
	List<ActionEntity> findByOrigin(String origin);

}
