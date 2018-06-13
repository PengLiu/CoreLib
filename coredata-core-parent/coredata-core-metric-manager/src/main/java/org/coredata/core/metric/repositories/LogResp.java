package org.coredata.core.metric.repositories;

import org.coredata.core.metric.documents.LogFile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LogResp extends ElasticsearchRepository<LogFile, String> {
	
}
