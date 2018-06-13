package org.coredata.core.data;



import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.dsl.Disruptor;

public class DefaultRecordCollector implements RecordCollector {

	private static final EventTranslatorOneArg<RecordEvent, Record> TRANSLATOR = new EventTranslatorOneArg<RecordEvent, Record>() {

		@Override
		public void translateTo(RecordEvent event, long sequence, Record record) {
			event.setRecord(record);
		}
	};

	private Disruptor<RecordEvent> disruptor;
	
	private Metric metric;

	public DefaultRecordCollector(Disruptor<RecordEvent> disruptor,Metric metric) {
		this.disruptor = disruptor;
		this.metric = metric;
	}

	@Override
	public void send(Record record) {
		disruptor.publishEvent(TRANSLATOR, record);
		metric.getReadCount().incrementAndGet();
	}

	@Override
	public void send(Record[] records) {
		for (Record record : records) {
            send(record);
        }
	}

}
