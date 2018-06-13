package org.coredata.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Metric {

	private AtomicLong readCount = new AtomicLong(0);
	private AtomicLong writeCount = new AtomicLong(0);
	private AtomicLong readBytes = new AtomicLong(0);

	private long readerStartTime;
	private long readerEndTime;
	private long writerStartTime;
	private long writerEndTime;
	private Record lastVal;
	private List<Record> preview = new ArrayList<>();

	public void addRecord(Record record) {
		preview.add(record);
	}

	public AtomicLong getReadCount() {
		return readCount;
	}

	public void setReadCount(AtomicLong readCount) {
		this.readCount = readCount;
	}

	public AtomicLong getWriteCount() {
		return writeCount;
	}

	public void setWriteCount(AtomicLong writeCount) {
		this.writeCount = writeCount;
	}

	public long getReaderStartTime() {
		return readerStartTime;
	}

	public void setReaderStartTime(long readerStartTime) {
		this.readerStartTime = readerStartTime;
	}

	public long getReaderEndTime() {
		return readerEndTime;
	}

	public void setReaderEndTime(long readerEndTime) {
		this.readerEndTime = readerEndTime;
	}

	public long getWriterStartTime() {
		return writerStartTime;
	}

	public void setWriterStartTime(long writerStartTime) {
		this.writerStartTime = writerStartTime;
	}

	public long getWriterEndTime() {
		return writerEndTime;
	}

	public void setWriterEndTime(long writerEndTime) {
		this.writerEndTime = writerEndTime;
	}

	public AtomicLong getReadBytes() {
		return readBytes;
	}

	public void setReadBytes(AtomicLong readBytes) {
		this.readBytes = readBytes;
	}

	public long getSpeed() {
		long distance = (System.currentTimeMillis() - this.readerStartTime) / 1000;
		if (distance == 0) {
			return 0;
		}
		return this.readBytes.get() / distance;
	}

	public Record getLastVal() {
		return lastVal;
	}

	public void setLastVal(Record lastVal) {
		this.lastVal = lastVal;
	}

	public List<Record> getPreview() {
		return preview;
	}

}
