package org.coredata.core.event;

import static org.junit.Assert.assertNotNull;

import org.coredata.core.TestApp;
import org.coredata.core.alarm.documents.Event;
import org.coredata.core.alarm.documents.EventType;
import org.coredata.core.alarm.repositories.EventResp;
import org.coredata.core.alarm.services.EventService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class EventServiceTest {

	@Autowired
	private EventService eventService;

	@Autowired
	private EventResp eventResp;

	@Before
	public void init() {
		Event event = new Event();
		event.setContent("event content");
		event.setEntityId("entity 001");
		event.setEventType(EventType.AnomalyDetection);
		event.setMetricId("metric001");
		event.setToken("token");
		event = eventService.save(event);
		
		event = eventResp.findByEventId(event.getEventId());
		assertNotNull(event);
	}

	@Test
	public void eventQueryTest() {
		
	}

	@After
	public void cleanUp() {
		eventResp.deleteAll();
	}

}
