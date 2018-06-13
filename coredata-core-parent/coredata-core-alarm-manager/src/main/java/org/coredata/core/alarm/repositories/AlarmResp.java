package org.coredata.core.alarm.repositories;

import org.coredata.core.alarm.documents.Alarm;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AlarmResp extends ElasticsearchRepository<Alarm, String> {

}
