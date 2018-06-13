package org.coredata.core.stream.alarm.consumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.stream.alarm.StateChangeDto;
import org.coredata.core.stream.service.AlarmCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueueManager implements InitializingBean, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(QueueManager.class);

	private final BlockingQueue<StateChangeDto> stateQueue = new LinkedBlockingQueue<>();

	private final BlockingQueue<Alarm> alarmQueue = new LinkedBlockingQueue<>();

	private static final ExecutorService executor = Executors.newFixedThreadPool(2);

	@Autowired
	private AlarmCenterService alarmCenterService;

	@Override
	public void destroy() throws Exception {
		executor.shutdownNow();
	}

	public void put(StateChangeDto dto) {
		try {
			stateQueue.put(dto);
		} catch (Exception e) {
			logger.error("Put " + dto.getState() + " error.", e);
		}
	}

	public void putAlarm(Alarm alarm) {
		try {
			alarmQueue.put(alarm);
		} catch (InterruptedException e) {
			logger.error("Put " + alarm.getContent() + " error.", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		executor.execute(new StateChangeConsumer(alarmCenterService, stateQueue));
		executor.execute(new AlarmConsumer(alarmCenterService, alarmQueue));
	}

}
