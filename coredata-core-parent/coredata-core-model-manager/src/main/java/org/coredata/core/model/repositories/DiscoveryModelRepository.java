package org.coredata.core.model.repositories;

import java.util.List;

import org.coredata.core.model.entities.DiscoveryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DiscoveryModelRepository extends CrudRepository<DiscoveryEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_discovery d WHERE d.dis_id = ?1 ")
	DiscoveryEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_discovery d WHERE d.dis_restype = ?1 ")
	List<DiscoveryEntity> findByRestype(String restype);

}
