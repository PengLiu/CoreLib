package org.coredata.core.olap.model.repositories;

import java.util.List;

import org.coredata.core.olap.model.entities.OlapModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OlapModelResp extends JpaRepository<OlapModel, String> {

	OlapModel findByName(String name);

	List<OlapModel> findByJobId(String jobId);

}
