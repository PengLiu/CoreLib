package org.coredata.core.model.repositories;

import java.util.List;

import org.coredata.core.model.entities.TransformEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TransformModelRepository extends CrudRepository<TransformEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_transform t WHERE t.transform_id = ?1 ")
	TransformEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_transform t WHERE t.transform_origin = ?1 ")
	List<TransformEntity> findByOrigin(String origin);

}
