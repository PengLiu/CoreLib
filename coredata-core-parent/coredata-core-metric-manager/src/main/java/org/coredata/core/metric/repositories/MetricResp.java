package org.coredata.core.metric.repositories;

import org.coredata.core.metric.documents.Metric;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MetricResp extends ElasticsearchRepository<Metric, String> {

}
