package org.coredata.core.model.repositories;

import org.coredata.core.model.entities.DevtypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DevtypeModelRepository extends CrudRepository<DevtypeEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_devtype d WHERE d.devsysobject_id = ?1 ")
	DevtypeEntity findBySysobjectid(String sysoid);

	@Query(nativeQuery = true, value = "SELECT * FROM t_devtype d WHERE d.dev_id = ?1 ")
	DevtypeEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_devtype d WHERE d.dev_model -> '$.resmodelid' = ?1 ")
	List<DevtypeEntity> findByRestypeId(String restypeId);

}
