package org.coredata.core.alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.ElasticsearchService;
import org.coredata.core.TestApp;
import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.alarm.documents.AlarmSource;
import org.coredata.core.alarm.documents.Event;
import org.coredata.core.alarm.repositories.AlarmResp;
import org.coredata.core.alarm.services.AlarmService;
import org.coredata.core.util.elasticsearch.querydsl.Constants;
import org.coredata.core.util.elasticsearch.querydsl.TimeRangeFilter;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class AlarmServiceTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private AlarmResp alarmResp;

	@Autowired
	private ElasticsearchService elasticsearchService;

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

		Thread.sleep(500);

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

	@Test
	public void rawQueryTest() throws IOException, InterruptedException, QuerydslException {
		JsonNode updateQueryRequest = mapper.readTree(AlarmServiceTest.class.getResourceAsStream("/update_query.json"));
		elasticsearchService.rawQuery("POST", "/alarm_*/_update_by_query", mapper.writeValueAsString(updateQueryRequest));
		Thread.sleep(5000);
		TimeRangeFilter timeRange = new TimeRangeFilter(System.currentTimeMillis() - 60 * 1000, System.currentTimeMillis());
		String queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.location\", \"value\": 1},"
				+ "\"pagination\": { \"size\": 10, \"page\": 1}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		Page<Alarm> alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(31, alarms.getContent().get(0).getLevel());

	}

	@Test
	public void alarmCountTest() throws QuerydslException, JsonProcessingException {

		long time = System.currentTimeMillis();

		Map<String, Object> props = new HashMap<>();

		for (int i = 0; i < 20; i++) {
			List<AlarmSource> alarmSources = new ArrayList<>();
			alarmSources.add(new AlarmSource("testentity" + i, "CPURate"));
			Alarm alarm = new Alarm();
			alarm.setAlarmRuleId(i % 2 == 0 ? "rule1" : "rule2");
			alarm.setAlarmSources(alarmSources);
			alarm.setLevel(i % 2 == 0 ? 1 : 2);
			alarm.setProps(props);
			alarm.setContent("Alarm content.");
			alarmService.save(alarm);
		}

		TimeRangeFilter timeRange = new TimeRangeFilter(time, System.currentTimeMillis());

		String queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"testentity\"}, \"timeRange\": "
				+ mapper.writeValueAsString(timeRange) + "}";
		Assert.assertEquals(20L, alarmService.countAlarmByCondition(queryStr).get(Constants.DEFAULT_COUNT));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"testentity\"}, \"timeRange\": "
				+ mapper.writeValueAsString(timeRange) + ","
				+ "\"aggregations\": [{\"type\": \"Term\", \"field\": \"alarmRuleId\"},{\"type\": \"Term\", \"field\": \"level\"}]}";

		assertEquals(10L, alarmService.countAlarmByCondition(queryStr).get("rule1"));
		assertEquals(10L, alarmService.countAlarmByCondition(queryStr).get("rule2"));
		assertEquals(10L, alarmService.countAlarmByCondition(queryStr).get("1"));
		assertEquals(10L, alarmService.countAlarmByCondition(queryStr).get("2"));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"testentity\"}, \"timeRange\": "
				+ mapper.writeValueAsString(timeRange) + ","
				+ "\"aggregations\": [{\"type\": \"Term\", \"field\": \"alarmRuleId\"},{\"type\": \"Term\", \"field\": \"level\"},{\"type\": \"Term\", \"field\": \"entityId.keyword\"}]}";
		assertNotNull(alarmService.countAlarmByCondition(queryStr));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"testentity\"}, \"timeRange\": "
				+ mapper.writeValueAsString(timeRange) + ","
				+ "\"aggregations\": {\"type\": \"DateHistogram\",\"interval\": \"2s\", \"subAggs\": [{\"type\": \"Term\", \"field\": \"alarmRuleId\"},{\"type\": \"Term\", \"field\": \"level\"}]}}";
		assertNotNull(alarmService.countAlarmByCondition(queryStr));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"testentity\"}, \"timeRange\": "
				+ mapper.writeValueAsString(timeRange) + "," + "\"aggregations\": {\"type\": \"DateHistogram\",\"interval\": \"2s\"}}";
		assertNotNull(alarmService.countAlarmByCondition(queryStr));

	}

	@Test
	public void alarmQueryTest() throws QuerydslException, JsonProcessingException {

		TimeRangeFilter timeRange = new TimeRangeFilter(System.currentTimeMillis() - 3600 * 1000, System.currentTimeMillis());

		String queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"entityId\", \"value\": \"entity001\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		Page<Alarm> alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());
		assertEquals(2, alarms.getContent().get(0).getEvents().size());
		assertEquals("user001", alarms.getContent().get(0).getProps().get("user"));
		assertEquals(2, Integer.valueOf(alarms.getContent().get(0).getProps().get("location").toString()).intValue());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"entityId\", \"value\": \"entity001\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"ASC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(1, Integer.valueOf(alarms.getContent().get(0).getProps().get("location").toString()).intValue());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.user\", \"value\": \"user001\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\":" + mapper.writeValueAsString(timeRange) + "}";

		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());
		assertEquals("user001", alarms.getContent().get(0).getProps().get("user"));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\":" + mapper.writeValueAsString(timeRange) + "}";

		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"level\"], \"direction\" : \"ASC\"}, \"timeRange\":" + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());
		assertEquals(2, alarms.getContent().get(0).getProps().get("location"));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"level\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());
		assertEquals(30, alarms.getContent().get(0).getLevel());

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"props.location\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());
		assertEquals(2, alarms.getContent().get(0).getProps().get("location"));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"props.location\"], \"direction\" : \"ASC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());
		assertEquals(1, alarms.getContent().get(0).getProps().get("location"));

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"props.user\", \"value\": \"user\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"props.location\"], \"direction\" : \"ASC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

		queryStr = "{\"filter\":{\"or\":[{\"ops\":\"eq\", \"field\": \"entityId\", \"value\": \"entity001\"},{\"ops\":\"eq\", \"field\": \"level\", \"value\": 2}]},"
				+ "\"pagination\": { \"size\": 10, \"page\": 1} , \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

		queryStr = "{\"filter\":{\"and\":[{\"ops\":\"eq\", \"field\": \"entityId\", \"value\": \"entity001\"},{\"ops\":\"eq\", \"field\": \"level\", \"value\": 2}]},"
				+ "\"pagination\": { \"size\": 10, \"page\": 1}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + " }";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(1, alarms.getContent().size());

		queryStr = "{\"filter\":{\"or\":[{\"ops\":\"eq\", \"field\": \"entityId\", \"value\": \"entity\"},{\"ops\":\"eq\", \"field\": \"level\", \"value\": 2}]},"
				+ "\"pagination\": { \"size\": 10, \"page\": 1}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + " }";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(1, alarms.getContent().size());

		queryStr = "{\"filter\":{\"or\":[{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"entity\"},{\"ops\":\"prefix\", \"field\": \"metricId\", \"value\": \"CPURate\"}]},"
				+ "\"pagination\": { \"size\": 10, \"page\": 1}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + " }";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"entityId\", \"value\": \"*tity*\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";

		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"entityId\", \"value\": \"?ntity00?\"}," + "\"pagination\": { \"size\": 10, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";

		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

	}

	@Test
	public void sortTest() throws InterruptedException, QuerydslException, JsonProcessingException {

		List<Event> events = new ArrayList<>();

		Map<String, Object> props = new HashMap<>();

		long startMs = System.currentTimeMillis();

		for (int i = 0; i < 10; i++) {

			props.put("groupStr", i % 2 == 0 ? 1 + "/xxx" : 2 + "/xxx");
			props.put("groupSize", i % 2 == 0 ? 1 : 2);
			props.put("index", i);
			props.put("val", 10);

			List<AlarmSource> alarmSources = new ArrayList<>();
			alarmSources.add(new AlarmSource("myentity" + i, "CPURate"));

			Alarm alarm = new Alarm();
			alarm.setAlarmSources(alarmSources);
			alarm.setLevel(i % 2 == 0 ? 1 : 2);
			alarm.setEvents(events);
			alarm.setProps(props);
			alarm.setContent("Alarm content.");
			Thread.sleep(500);
			alarmService.save(alarm);
		}

		TimeRangeFilter timeRange = new TimeRangeFilter(System.currentTimeMillis() - 3600 * 1000, System.currentTimeMillis());

		String queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"myentity\"},"
				+ "\"pagination\": { \"size\": 5, \"page\": 1} ," + "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": "
				+ mapper.writeValueAsString(timeRange) + "}";

		Page<Alarm> alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(5, alarms.getContent().size());
		assertEquals(10, alarms.getTotalElements());
		assertEquals("myentity9", alarms.getContent().get(0).getAlarmSources().iterator().next().getEntityId());

		timeRange = new TimeRangeFilter(startMs, startMs + 1000);
		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"myentity\"}," + "\"pagination\": { \"size\": 5, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange) + "}";
		alarms = alarmService.findAlarmByCondition(queryStr);
		assertEquals(2, alarms.getContent().size());

		timeRange = new TimeRangeFilter(startMs, System.currentTimeMillis());
		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"myentity\"}," + "\"pagination\": { \"size\": 5, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange)
				+ ", \"aggregations\": {\"type\": \"DateHistogram\",\"interval\": \"2s\"}}";
		Map<String, Object> countResult = alarmService.countAlarmByCondition(queryStr);
		assertTrue(!countResult.isEmpty());

		queryStr = "{\"filter\":{\"ops\":\"prefix\", \"field\": \"entityId\", \"value\": \"myentity\"}," + "\"pagination\": { \"size\": 5, \"page\": 1} ,"
				+ "\"sort\": { \"fields\": [\"createdTime\"], \"direction\" : \"DESC\"}, \"timeRange\": " + mapper.writeValueAsString(timeRange)
				+ ", \"aggregations\": {\"type\": \"DateHistogram\",\"interval\": \"2s\" ,\"sugAggs\": {\"type\": \"Term\", \"field\":\"level\" }}}";
		countResult = alarmService.countAlarmByCondition(queryStr);
		assertNotNull(countResult);

	}

	@After
	public void cleanup() {
		alarmResp.deleteAll();
	}

}