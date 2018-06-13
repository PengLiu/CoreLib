package org.coredata.core.model.repositories;

import java.util.List;

import org.coredata.core.model.entities.RestypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RestypeRepository extends CrudRepository<RestypeEntity, Long> {

	@Query(nativeQuery = true, value = "SELECT * FROM t_restype r WHERE r.restype_id = ?1 ")
	RestypeEntity findById(String id);

	@Query(nativeQuery = true, value = "SELECT * FROM t_restype r WHERE r.res_parent_id = ?1 AND r.isroot = ?2 AND ( r.customerId is null OR r.customerId = '' OR r.customerId like ?3)")
	List<RestypeEntity> findByParentidAndIsroot(String id, String isroot, String customerId);

	@Query(nativeQuery = true, value = "SELECT * FROM t_restype r WHERE r.res_parent_id = ?1 AND ( r.customerId is null OR r.customerId = '' OR r.customerId like ?2)")
	List<RestypeEntity> findByParentid(String id, String customerId);

	@Query(nativeQuery = true, value = "SELECT * FROM t_restype r WHERE r.res_parent_id = ?1 AND r.isroot = ?2 AND r.onlyclassify = ?3 AND ( r.customerId is null OR r.customerId = '' OR r.customerId like ?4)")
	List<RestypeEntity> findByParentidAndIsrootAndOnlyclassify(String id, String isroot, boolean onlyclassify, String customerId);

}
