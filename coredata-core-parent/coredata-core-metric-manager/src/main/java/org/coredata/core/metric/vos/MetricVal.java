package org.coredata.core.metric.vos;

public class MetricVal {

	private long dateTime;

	private double avg;

	private double max;

	private double min;

	public MetricVal(long dateTime) {
		this.dateTime = dateTime;
	}

	public MetricVal(long dateTime, double avg, double max, double min) {
		this.dateTime = dateTime;
		this.avg = avg;
		this.max = max;
		this.min = min;
	}

	public long getDateTime() {
		return dateTime;
	}

	public double getAvg() {
		return avg;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public void setDateTime(long dateTime) {
		this.dateTime = dateTime;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public void setMin(double min) {
		this.min = min;
	}

}
