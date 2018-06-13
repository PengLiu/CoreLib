package org.coredata.core.model.repositories;

import org.coredata.core.model.entities.VendorEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VendorRepository extends CrudRepository<VendorEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_vendor v WHERE v.vendor_id = ?1 ")
	VendorEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_vendor v WHERE JSON_CONTAINS(v.vendor_model->'$.restypes',?1) ")
	List<VendorEntity> findByRestype(String restype);

}
