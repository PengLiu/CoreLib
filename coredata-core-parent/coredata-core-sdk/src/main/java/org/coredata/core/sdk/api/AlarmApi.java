package org.coredata.core.sdk.api;

import java.util.List;

import org.coredata.core.ElasticsearchService;
import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.alarm.services.AlarmService;
import org.elasticsearch.client.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmApi {

	@Autowired
	private AlarmService alarmService;
	@Autowired
	private ElasticsearchService elasticsearchService;

	@PutMapping("/")
	public Alarm saveAlarm(@RequestBody Alarm alarm) {
		return alarmService.save(alarm);
	}

	@Async
	@PutMapping(path = "/all")
	public void saveAlarms(@RequestBody List<Alarm> alarms) {
		alarmService.save(alarms);
	}

//	@GetMapping(path = "/count")
//	public Map<String, Object> countByCondition(@RequestBody AlarmCountConfidtion condition) {
//		TimeRange timeRange = new TimeRange(condition.getStartTime(), condition.getEndTime(), condition.getTimeField(), condition.getInterval(),
//				condition.getAggregationType(), condition.getGroupBy());
//		return alarmService.countAlarmByProps(condition.getSearchCondition(), condition.getOperation(), condition.getFuzzy(), timeRange);
//	}
//
//	@PostMapping(path = "/query")
//	public Page<Alarm> searchByCondition(@RequestBody AlarmSearchCondition condition) {
//		TimeRange timeRange = new TimeRange(condition.getStartTime(), condition.getEndTime());
//		Pageable pageable = PageRequest.of(condition.getPage(), condition.getPageSize(), Direction.valueOf(condition.getSort()), condition.getSortField());
//		return alarmService.findAlarmByProps(condition.getSearchCondition(), pageable, condition.getOperation(), condition.getFuzzy(), timeRange);
//	}

	@GetMapping(path = "/")
	public Response executeScript(String scriptJson) {
		return elasticsearchService.rawQuery("POST", "/alarm-*/_update_by_query", scriptJson);
	}
}
