package org.coredata.core.stream.alarm.consumer;

import java.util.concurrent.BlockingQueue;

import org.coredata.core.stream.alarm.StateChangeDto;
import org.coredata.core.stream.service.AlarmCenterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateChangeConsumer implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(StateChangeConsumer.class);

	private BlockingQueue<StateChangeDto> stateQueue;

	private AlarmCenterService alarmCenterService;

	public StateChangeConsumer(AlarmCenterService alarmCenterService, BlockingQueue<StateChangeDto> stateQueue) {
		this.alarmCenterService = alarmCenterService;
		this.stateQueue = stateQueue;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				StateChangeDto changeStates = stateQueue.take();
				if (logger.isDebugEnabled())
					logger.debug("Receive state change data is:" + changeStates.getState());
				alarmCenterService.processStateChange(changeStates);
			} catch (Exception e) {
				logger.error("Receive state data error.", e);
			}
		}
	}

}
