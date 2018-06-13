package org.coredata.core.repository;

import org.coredata.core.entities.TestData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TestResp extends ElasticsearchRepository<TestData, String> {

}
