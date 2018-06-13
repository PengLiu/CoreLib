package org.coredata.core.alarm.services;

import java.util.List;

import org.coredata.core.alarm.documents.Event;
import org.coredata.core.alarm.repositories.EventResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventService {

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private EventResp eventResp;

	public Event save(Event event) {
		return eventResp.save(event);
	}

	public void saveAll(List<Event> events) {
		eventResp.saveAll(events);
	}
}
