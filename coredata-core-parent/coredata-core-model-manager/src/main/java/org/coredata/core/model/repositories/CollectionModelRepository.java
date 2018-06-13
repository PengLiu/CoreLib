package org.coredata.core.model.repositories;

import java.util.List;

import org.coredata.core.model.entities.CollectionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CollectionModelRepository extends CrudRepository<CollectionEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_collection c WHERE c.col_id = ?1 ")
	CollectionEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_collection c WHERE c.col_restype = ?1 ")
	List<CollectionEntity> findByRestype(String restype);

	@Query(nativeQuery = true, value = "SELECT * FROM t_collection c WHERE c.col_origin = ?1 ")
	List<CollectionEntity> findByOrigin(String origin);

}
