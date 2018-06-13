package org.coredata.core.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.TestApp;
import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.alarm.documents.AlarmSource;
import org.coredata.core.alarm.documents.Event;
import org.coredata.core.alarm.repositories.AlarmResp;
import org.coredata.core.alarm.services.AlarmService;
import org.coredata.core.util.querydsl.DSLBuilder;
import org.coredata.core.util.querydsl.FilterGroupBuilder;
import org.coredata.core.util.querydsl.LogicOps;
import org.coredata.core.util.querydsl.QueryOps;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class AlarmServiceDSLTest {

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private AlarmResp alarmResp;

	@Before
	public void init() throws InterruptedException {

		List<Event> events = new ArrayList<>();

		Event event = new Event();
		event.setEventId("event01");
		events.add(event);
		event.setEventId("event02");
		events.add(event);

		List<AlarmSource> alarmSources = new ArrayList<>();
		alarmSources.add(new AlarmSource("entity001", "CPURate"));

		Map<String, Object> props = new HashMap<>();
		props.put("user", "user001");
		props.put("location", 1);
		props.put("manager", "bob");

		Alarm alarm = new Alarm();
		alarm.setAlarmSources(alarmSources);
		alarm.setLevel(30);
		alarm.setEvents(events);
		alarm.setProps(props);
		alarm.setContent("Alarm content.");
		alarmService.save(alarm);

		props.put("manager", "alice");
		props.put("location", 2);
		alarm = new Alarm();
		alarm.setAlarmSources(alarmSources);
		alarm.setLevel(2);
		alarm.setEvents(events);
		alarm.setProps(props);
		alarm.setContent("Alarm content xxx.");
		alarmService.save(alarm);

	}

	@After
	public void cleanup() {
		alarmResp.deleteAll();
	}

	@Test
	public void dslQueryTest() throws QuerydslException {

		String queryStr = DSLBuilder.instance().filter(LogicOps.and)
				.filterGroup(FilterGroupBuilder.instance().filterGroup().filter(QueryOps.eq, "level", 30).filter(QueryOps.eq, "props.user", "user001")).build();
		Page<Alarm> page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());

		queryStr = DSLBuilder.instance().filter(LogicOps.and)
				.filterGroup(FilterGroupBuilder.instance().filterGroup().filter(QueryOps.eq, "level", 2).filter(QueryOps.eq, "props.user", "user001")).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());

		queryStr = DSLBuilder.instance().filter(LogicOps.and)
				.filterGroup(FilterGroupBuilder.instance().filterGroup().filter(QueryOps.gte, "level", 2).filter(QueryOps.eq, "props.user", "user001")).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(2, page.getContent().size());

		queryStr = DSLBuilder.instance().filter(LogicOps.and).filterGroup(FilterGroupBuilder.instance().filterGroup().filter(QueryOps.gte, "level", 2)
				.filter(QueryOps.eq, "props.user", "user001").filter(QueryOps.gte, "props.location", 2)).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());
		

		queryStr = DSLBuilder.instance().filter(QueryOps.eq, "level", 30).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());
		
		queryStr = DSLBuilder.instance().filter(QueryOps.eq, "level", 2).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());
		
		queryStr = DSLBuilder.instance().filter(QueryOps.gt, "level", 2).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());
		
		queryStr = DSLBuilder.instance().filter(QueryOps.gte, "level", 2).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(2, page.getContent().size());
		
		queryStr = DSLBuilder.instance().filter(QueryOps.gte, "level", 2).build();
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(2, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.user\", \"value\": \"user001\"}}";
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(2, page.getContent().size());

		queryStr = "	{ \"filter\":{\"and\": [{\"ops\":\"eq\", \"field\": \"level\", \"value\": 30},{\"ops\":\"eq\", \"field\": \"props.user\", \"value\": \"user001\"}]}}";
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(1, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user001\"}}";
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(2, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.user\", \"value\": \"user002\"}}";
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(0, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"ser001\"}}";
		page = alarmService.findAlarmByCondition(queryStr);
		Assert.assertEquals(0, page.getContent().size());

	}

}
