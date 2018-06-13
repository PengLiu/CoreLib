package org.coredata.core.metric.repositories;

import org.coredata.core.metric.documents.WebOpinion;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface WebOpinionResp extends ElasticsearchRepository<WebOpinion, String> {
	
}
