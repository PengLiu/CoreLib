package org.coredata.core.model.repositories;

import org.coredata.core.model.entities.VendorTypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VendorTypeRepository extends CrudRepository<VendorTypeEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_vendortype v WHERE v.restype = ?1 ")
	List<VendorTypeEntity> findByRestype(String restype);

}
