package org.coredata.core.data.repositories;

import org.coredata.core.data.entities.SqlModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SqlModelResp extends PagingAndSortingRepository<SqlModel,String>{
	
	
}
