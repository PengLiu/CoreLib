package org.coredata.core.data;

import org.coredata.core.data.writers.debug.DebugWriter;

import com.lmax.disruptor.WorkHandler;

public class RecordWorkerHandler implements WorkHandler<RecordEvent> {

	public Writer writer;

	private Metric metric;

	public RecordWorkerHandler(Writer writer, Metric metric) {
		this.writer = writer;
		this.metric = metric;
	}

	@Override
	public void onEvent(RecordEvent event) throws Exception {
		writer.execute(event.getRecord());
		metric.getWriteCount().incrementAndGet();
		metric.setLastVal(event.getRecord());
		if (writer instanceof DebugWriter) {
			metric.addRecord(event.getRecord());
		}
	}

}
