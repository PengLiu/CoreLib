package org.coredata.core.olap.model.repositories;

import org.coredata.core.olap.model.entities.ExpensesRecord;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ExpensesRecordResp extends ElasticsearchRepository<ExpensesRecord, String> {

}
