package org.coredata.core.data;

public interface RecordCollector {

	public void send(Record record);

	public void send(Record[] records);

}
