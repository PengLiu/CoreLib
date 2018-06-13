package org.coredata.core.data.debugger;

import java.util.ArrayList;
import java.util.List;

import org.coredata.core.data.Fields;
import org.coredata.core.data.Record;


public class JobDetail {

	private String readerSpentMs = null;
	private String writerSpentMs = null;

	private String readerSpeed;
	private String writerSpeed;

	private long readerCount = 0;
	private long writerCount = 0;

	private Record lastVal;

	private Fields fields = new Fields();

	private JobStatus status = JobStatus.Running;

	private final List<Record> records = new ArrayList<>();

	public void addRecord(Record record) {
		records.add(record);
	}

	public List<Record> getRecords() {
		return records;
	}

	public long getReaderCount() {
		return readerCount;
	}

	public void setReaderCount(long readerCount) {
		this.readerCount = readerCount;
	}

	public long getWriterCount() {
		return writerCount;
	}

	public void setWriterCount(long writerCount) {
		this.writerCount = writerCount;
	}

	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public String getReaderSpentMs() {
		return readerSpentMs;
	}

	public void setReaderSpentMs(String readerSpentMs) {
		this.readerSpentMs = readerSpentMs;
	}

	public String getWriterSpentMs() {
		return writerSpentMs;
	}

	public void setWriterSpentMs(String writerSpentMs) {
		this.writerSpentMs = writerSpentMs;
	}

	public String getReaderSpeed() {
		return readerSpeed;
	}

	public void setReaderSpeed(String readerSpeed) {
		this.readerSpeed = readerSpeed;
	}

	public String getWriterSpeed() {
		return writerSpeed;
	}

	public void setWriterSpeed(String writerSpeed) {
		this.writerSpeed = writerSpeed;
	}

	public Record getLastVal() {
		return lastVal;
	}

	public void setLastVal(Record lastVal) {
		this.lastVal = lastVal;
	}

	@Override
	public String toString() {
		return "JobDetail [readerSpentMs=" + readerSpentMs + ", writerSpentMs=" + writerSpentMs + ", readerSpeed="
				+ readerSpeed + ", writerSpeed=" + writerSpeed + ", readerCount=" + readerCount + ", writerCount="
				+ writerCount + ", lastVal=" + lastVal + ", status=" + status + ", records=" + records + "]";
	}

	public Fields getFields() {
		return fields;
	}

	public void setFields(Fields fields) {
		this.fields = fields;
	}

}
