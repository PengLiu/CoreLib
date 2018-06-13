package org.coredata.core.data.repositories;

import java.util.List;

import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.entities.DataSourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportJobResp extends JpaRepository<DataImportJob, String> {

	Page<DataImportJob> findByTokenAndNameContaining(String token, String name, Pageable pageable);

	Page<DataImportJob> findByTokenAndDataSourceTypeIn(String token, List<DataSourceType> types, Pageable pageable);

	Page<DataImportJob> findByTokenAndNameContainingAndTypeIn(String token, String name, List<String> types, Pageable pageable);

	Page<DataImportJob> findByTokenAndNameContainingAndDataSourceTypeIn(String token, String name,
			List<DataSourceType> dataSourceTypes, Pageable pageable);

	Page<DataImportJob> findByTokenAndNameContainingAndDataSourceTypeInAndTypeIn(String token, String name,
			List<DataSourceType> dataSourceTypes, List<String> types, Pageable pageable);
			
	Page<DataImportJob> findByTokenAndNameContainingAndDataSourceTypeInAndTypeInAndIndexNameIsNotNull(String token,
			String nameParam, List<DataSourceType> ts, List<String> typeParam, Pageable pageable);

	Page<DataImportJob> findByTokenAndNameContainingAndDataSourceTypeInAndIndexNameIsNotNull(String token,
			String nameParam, List<DataSourceType> ts, Pageable pageable);

	Page<DataImportJob> findByTokenAndNameContainingAndTypeInAndIndexNameIsNotNull(String token, String nameParam,
			List<String> typeParam, Pageable pageable);

	Page<DataImportJob> findByTokenAndNameContainingAndIndexNameIsNotNull(String token, String nameParam,
			Pageable pageable);
}
