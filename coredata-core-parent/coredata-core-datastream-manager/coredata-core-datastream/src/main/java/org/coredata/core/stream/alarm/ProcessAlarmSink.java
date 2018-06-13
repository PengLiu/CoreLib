package org.coredata.core.stream.alarm;

import java.util.List;

import org.coredata.core.model.decision.Action;
import org.coredata.core.stream.alarm.consumer.QueueManager;
import org.coredata.core.stream.service.AlarmCenterService;
import org.coredata.core.stream.vo.AlarmData;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

import akka.stream.Attributes;
import akka.stream.Inlet;
import akka.stream.SinkShape;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class ProcessAlarmSink<A> extends GraphStage<SinkShape<A>> {

	private static Logger logger = LoggerFactory.getLogger(ProcessAlarmSink.class);

	public final Inlet<A> in = Inlet.create("ProcessAlarmSink.in");

	private final SinkShape<A> shape = SinkShape.of(in);

	private QueueManager queueManager;

	private AlarmCenterService alarmService;

	private RedisService redisService;

	public ProcessAlarmSink(QueueManager queueManager, AlarmCenterService alarmService, RedisService redisService) {
		this.queueManager = queueManager;
		this.alarmService = alarmService;
		this.redisService = redisService;
	}

	@Override
	public SinkShape<A> shape() {
		return shape;
	}

	@Override
	public GraphStageLogic createLogic(Attributes attr) throws Exception {

		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {
					@Override
					public void onPush() throws Exception {
						A alarm = grab(in);
						try {
							if (logger.isDebugEnabled())
								logger.debug("Receive alarm data : " + alarm.toString());
							AlarmData alarmData = JSON.parseObject(alarm.toString(), AlarmData.class);
							List<Action> actions = alarmData.getActions();
							if (!CollectionUtils.isEmpty(actions)) {
								boolean alarmResult = alarmData.getAlarmResult();
								for (Action action : actions) {
									switch (action.getType()) {
									case "stateTransition":
										long starttime = System.currentTimeMillis();
										StateChange.instance().stateChange(queueManager, alarmResult, alarmData.getInstances(), redisService);
										long endtime = System.currentTimeMillis();
										logger.info("==============================change state 耗时:" + (endtime - starttime));
										break;
									case "sendAlarm":
										if ("false".equals(alarmData.getEnable()))
											continue;
										long starttime1 = System.currentTimeMillis();
										AlarmAction alarmAction = new AlarmAction(alarmData.getRuleId(), alarmResult, alarmData.getExpConditionStr(), action,
												alarmData.getInstances(), alarmData.getMetric(), alarmData.getMetricValue());
										AlarmHelper.instance().fire(queueManager, alarmService, redisService, alarmAction);
										long endtime1 = System.currentTimeMillis();
										logger.info("==============================save alarm 耗时:" + (endtime1 - starttime1));
										break;
									}
								}
							}
						} catch (Exception e) {
							logger.error("Send alarm error.", e);
						} finally {
							pull(in);
						}
					}
				});
			}

			@Override
			public void preStart() {
				pull(in);
			}
		};

	}

}
