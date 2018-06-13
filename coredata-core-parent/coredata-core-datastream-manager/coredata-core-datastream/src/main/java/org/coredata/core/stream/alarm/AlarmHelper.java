package org.coredata.core.stream.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.alarm.documents.AlarmSource;
import org.coredata.core.model.decision.Param; 
import org.coredata.core.stream.alarm.consumer.QueueManager;
import org.coredata.core.stream.service.AlarmCenterService;
import org.coredata.core.util.common.MethodUtil;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmHelper {

	private static final AlarmHelper helper = new AlarmHelper();

	private Logger logger = LoggerFactory.getLogger(AlarmHelper.class);

	private ConcurrentHashMap<String, AlarmAction> alarms = new ConcurrentHashMap<>();

	private AlarmHelper() {

	}

	public static final AlarmHelper instance() {
		return helper;
	}

	private void sendAlarm(QueueManager manager, Alarm alarm) {
		try {
			manager.putAlarm(alarm);
		} catch (Exception e) {
			logger.error("Save alarm error", e);
		}
	}

	public void fire(QueueManager manager, AlarmCenterService alarmService, RedisService redisService, AlarmAction alarm) {
		AlarmAction tmp = alarms.get(alarm.getAlarmId());
		if (alarm.isExpResult()) {
			if (tmp == null)
				alarms.put(alarm.getAlarmId(), alarm);
			tmp = alarms.get(alarm.getAlarmId());
			if (tmp.fire()) {//拼接告警实体
				alarms.remove(tmp.getAlarmId());
				final Alarm alarmEntity = createAlarm(alarm);
				Map<String, Object> props = new HashMap<>();
				props.put("recovered", false);
				alarmEntity.setProps(props);
				sendAlarm(manager, alarmEntity);
			}
		} else {
			//表达式失败 考虑是否重置flapping
			if (tmp != null) {
				if ("consecutive".equals(tmp.getFlapping().getType())) {
					tmp.reset();
				}
			}
			final Alarm alarmEntity = createAlarm(alarm);
			Map<String, Object> props = new HashMap<>();
			props.put("recovered", true);
			alarmEntity.setProps(props);
			sendAlarm(manager, alarmEntity);
		}
	}

	private Alarm createAlarm(AlarmAction alarm) {
		final Alarm alarmEntity = new Alarm();
		String entityId = alarm.getRelatedInstances();
		List<String> metrics = alarm.getMetric();
		Collections.sort(metrics);
		List<AlarmSource> sources = new ArrayList<>();
		StringBuilder alarmRule = new StringBuilder();
		alarmRule.append(entityId);
		for (String metric : metrics) {
			alarmRule.append(metric);
			AlarmSource source = new AlarmSource(entityId, metric);
			sources.add(source);
		}
		String alarmRuleId = MethodUtil.md5(alarmRule.toString());
		alarmEntity.setAlarmRuleId(alarmRuleId);
		alarmEntity.setLevel(Integer.parseInt(alarm.getLevel()));
		alarmEntity.setAlarmSources(sources);
		Optional<Param> param = alarm.getParam().stream().filter(p -> "content".equals(p.getKey())).findAny();
		if (param.isPresent()) {
			Param p = param.get();
			alarmEntity.setContent(p.getValue());
		} else
			alarmEntity.setContent("");
		alarmEntity.setCreatedTime(System.currentTimeMillis());
		return alarmEntity;
	}

}
