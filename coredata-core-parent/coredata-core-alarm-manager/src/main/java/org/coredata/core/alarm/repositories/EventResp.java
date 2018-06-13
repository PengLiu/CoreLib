package org.coredata.core.alarm.repositories;

import org.coredata.core.alarm.documents.Event;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EventResp extends ElasticsearchRepository<Event, String> {

	Event findByEventId(String eventId);

}
