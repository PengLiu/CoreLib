package org.coredata.core.olap.model.repositories;

import org.coredata.core.olap.model.entities.Wristband;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface WristbandRest extends ElasticsearchRepository<Wristband, String> {

}
