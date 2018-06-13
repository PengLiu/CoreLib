package org.coredata.core.stream.alarm.consumer;

import java.util.concurrent.BlockingQueue;

import org.coredata.core.alarm.documents.Alarm;
import org.coredata.core.stream.service.AlarmCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmConsumer implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(AlarmConsumer.class);

	private BlockingQueue<Alarm> alarmQueue;

	private AlarmCenterService alarmService;

	public AlarmConsumer(AlarmCenterService alarmService, BlockingQueue<Alarm> alarmQueue) {
		this.alarmService = alarmService;
		this.alarmQueue = alarmQueue;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Alarm alarm = alarmQueue.take();
				if (logger.isDebugEnabled())
					logger.debug("Receive alarm data is:" + alarm.getContent());
				alarmService.processAlarm(alarm);
			} catch (Exception e) {
				logger.error("process alarm data error.", e);
			}
		}
	}

}
