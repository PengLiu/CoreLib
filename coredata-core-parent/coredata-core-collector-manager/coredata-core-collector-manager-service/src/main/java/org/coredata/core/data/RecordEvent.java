package org.coredata.core.data;

import com.lmax.disruptor.EventFactory;

public class RecordEvent {

	private Record record;

	public Record getRecord() {
		return record;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	public static final EventFactory<RecordEvent> FACTORY = new EventFactory<RecordEvent>() {

		public RecordEvent newInstance() {
			return new RecordEvent();
		}
	};

}
