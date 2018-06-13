package org.coredata.core.model.repositories;

import java.util.List;

import org.coredata.core.model.entities.MiningEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DataminingModelRepository extends CrudRepository<MiningEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_mining m WHERE m.mining_id = ?1 ")
	MiningEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_mining m WHERE m.mining_origin = ?1 ")
	List<MiningEntity> findByOrigin(String origin);

}
