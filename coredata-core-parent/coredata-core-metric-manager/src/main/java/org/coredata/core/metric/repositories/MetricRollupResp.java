package org.coredata.core.metric.repositories;

import org.coredata.core.metric.documents.MetricRollup;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MetricRollupResp extends ElasticsearchRepository<MetricRollup, String> {

}
