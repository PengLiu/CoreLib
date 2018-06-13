package org.coredata.core.model.repositories;

import org.coredata.core.model.entities.MetricEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MetricRepository extends CrudRepository<MetricEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_metric m WHERE m.metric_id = ?1 ")
	MetricEntity findById(String id);

}
