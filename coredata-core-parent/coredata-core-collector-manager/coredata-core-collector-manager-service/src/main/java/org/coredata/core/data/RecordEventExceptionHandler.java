package org.coredata.core.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.Disruptor;

public class RecordEventExceptionHandler implements ExceptionHandler<Object> {

	private final Disruptor<RecordEvent> disruptor;
	private final JobContext jobContext;
	private static Logger LOGGER = LogManager.getLogger(RecordEventExceptionHandler.class);

	public RecordEventExceptionHandler(Disruptor<RecordEvent> disruptor, JobContext context) {
		this.disruptor = disruptor;
		this.jobContext = context;
	}

	public void handleEventException(Throwable t, long sequence, Object event) {
		LOGGER.error(Throwables.getStackTraceAsString(t));
		jobContext.setWriterError(true);
		disruptor.shutdown();
	}

	public void handleOnShutdownException(Throwable t) {
		LOGGER.error(Throwables.getStackTraceAsString(t));
		disruptor.shutdown();
	}

	public void handleOnStartException(Throwable t) {
		LOGGER.error(Throwables.getStackTraceAsString(t));
		disruptor.shutdown();
	}
}
