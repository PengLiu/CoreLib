package org.coredata.core.stream.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.alarm.services.AlarmService;
import org.coredata.core.entities.ResEntity;
import org.coredata.core.entities.repositories.EntityResp;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.stream.alarm.StateChangeDto;
import org.coredata.core.util.event.Event;
import org.coredata.core.util.event.IEvent;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AlarmCenterService {

	private static final Logger logger = LoggerFactory.getLogger(AlarmCenterService.class);

	@Value("${spring.redis.pub.state: events}")
	private String pubTopic = "events";

	@Value("${spring.redis.pub.alarm: alarm}")
	private String alarmTopic = "alarm";

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private RedisService redisService;

	@Autowired
	private MetricService metricService;

	@Autowired
	private AlarmService alarmService;

	@Autowired
	private EntityResp entityResp;

	private ChannelTopic alarmPubTopic = new ChannelTopic(alarmTopic);

	private ChannelTopic stateChangeTopic = new ChannelTopic(pubTopic);

	/**
	 * 用于更新资产状态
	 * @param params
	 */
	public void processStateChange(StateChangeDto dto) {
		List<ResEntity> insts = new ArrayList<>();
		List<String> ids = dto.getInstIds();
		String state = dto.getState();
		for (String instId : ids) {
			ResEntity instance = (ResEntity) redisService.loadDataByTableAndKey(RedisService.INSTANCE, instId);
			if (instance == null)
				continue;
			//调整相关逻辑，此处接收到的消息均为需要更改状态的消息
			if (instance != null) {
				Map<String, Object> props = instance.getProps();
				Object level = props.get("nodeLevel");
				if (level == null)
					continue;
				if ("root".equals(level.toString())) {
					//保存可用性指标
					Metric metric = new Metric();
					metric.setMetricId("AvailableStatus");
					metric.setToken(instance.getToken());
					metric.setEntityId(instance.getEntityId());
					metric.setStringVal(state);
					metric.setCreatedTime(System.currentTimeMillis());
					metricService.save(metric);
				}
				instance.setStatus(state);
				insts.add(instance);
				ObjectNode content = mapper.createObjectNode();
				content.put("instId", instId);
				content.put("state", state);
				try {
					IEvent event = new Event(IEvent.Type.StateChanged, mapper.writeValueAsString(content));
					redisService.publish(stateChangeTopic, mapper.writeValueAsString(event));
					if (logger.isDebugEnabled()) {
						logger.debug(" Instance " + instId + " state is " + state + ":" + "success");
					}
				} catch (IOException e) {
					logger.error("Change " + instId + " state error", e);
				}
			}
		}
		if (insts.size() > 0)
			batchSaveInstance(insts);

	}

	private void batchSaveInstance(List<ResEntity> insts) {
		try {
			entityResp.saveAll(insts);
			for (ResEntity inst : insts)
				redisService.saveData(RedisService.INSTANCE, inst.getEntityId(), inst);
		} catch (Exception e) {
			logger.error("Batch save instance error.", e);
		}

	}

	public void processAlarm(Alarm alarm) {
		try {
			//存储alarm
			alarmService.save(alarm);
			redisService.publish(alarmPubTopic, mapper.writeValueAsString(alarm));
		} catch (JsonProcessingException e) {
			logger.error("Send alarm error.", e);
		}
	}

}
