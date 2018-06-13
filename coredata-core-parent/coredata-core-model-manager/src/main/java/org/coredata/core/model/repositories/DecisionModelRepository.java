package org.coredata.core.model.repositories;

import java.util.List;

import org.coredata.core.model.entities.DecisionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DecisionModelRepository extends CrudRepository<DecisionEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_decision d WHERE d.decision_id = ?1 ")
	DecisionEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_decision d WHERE d.decision_origin = ?1 ")
	List<DecisionEntity> findByOrigin(String origin);

}
