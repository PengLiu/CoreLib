package org.coredata.core.datastream.blueprint.vo;

import java.util.concurrent.TimeUnit;

import org.coredata.core.datastream.blueprint.exception.BluePrintException;

import scala.concurrent.duration.FiniteDuration;

public class BatchedFragment {

	protected BatchType batchType = BatchType.None;

	protected int batchSize = -1;

	protected int step;

	protected FiniteDuration timeout;

	protected FiniteDuration infinityTime = FiniteDuration.apply(365, TimeUnit.DAYS);

	public void setTimeOut(long timeOutInSeconds) throws BluePrintException {
		if (timeOutInSeconds > 15 * 60 * 1000 || timeOutInSeconds <= 0) {
			throw new BluePrintException("Time out illegle, time out must be less than 15 mins.");
		}
		timeout = FiniteDuration.apply(timeOutInSeconds, TimeUnit.SECONDS);
	}

	public BatchType getBatchType() {
		return batchType;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchType(BatchType batchType) {
		this.batchType = batchType;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public FiniteDuration getInfinityTime() {
		return infinityTime;
	}

	public FiniteDuration getTimeout() {
		return timeout;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

}