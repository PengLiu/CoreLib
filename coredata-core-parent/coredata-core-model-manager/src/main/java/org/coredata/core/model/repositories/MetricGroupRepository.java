package org.coredata.core.model.repositories;

import org.coredata.core.model.entities.MetricGroupEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MetricGroupRepository extends CrudRepository<MetricGroupEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_metricgroup mg WHERE mg.metricgroup_id = ?1 ")
	MetricGroupEntity findById(String id);

}
