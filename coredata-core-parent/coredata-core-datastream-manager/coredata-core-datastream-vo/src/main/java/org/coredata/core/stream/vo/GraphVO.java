package org.coredata.core.stream.vo;

import java.io.Serializable;

/**
 * 用于拼接流程图vo对象，包含所需基本属性
 * @author sue
 *
 */
public class GraphVO implements Serializable {

	private static final long serialVersionUID = -4871016558038436382L;

	private int parallelNum;

	private boolean saveTransform;

	private boolean saveMetric;

	private String dstransformTopic;

	private String dsmetricTopic;

	private String kafkaAddr;

	public GraphVO(int parallelNum, boolean saveTransform, boolean saveMetric, String dstransformTopic, String dsmetricTopic, String kafkaAddr) {
		this.parallelNum = parallelNum;
		this.saveTransform = saveTransform;
		this.saveMetric = saveMetric;
		this.dstransformTopic = dstransformTopic;
		this.dsmetricTopic = dsmetricTopic;
		this.kafkaAddr = kafkaAddr;
	}

	public int getParallelNum() {
		return parallelNum;
	}

	public void setParallelNum(int parallelNum) {
		this.parallelNum = parallelNum;
	}

	public boolean isSaveTransform() {
		return saveTransform;
	}

	public void setSaveTransform(boolean saveTransform) {
		this.saveTransform = saveTransform;
	}

	public boolean isSaveMetric() {
		return saveMetric;
	}

	public void setSaveMetric(boolean saveMetric) {
		this.saveMetric = saveMetric;
	}

	public String getDstransformTopic() {
		return dstransformTopic;
	}

	public void setDstransformTopic(String dstransformTopic) {
		this.dstransformTopic = dstransformTopic;
	}

	public String getDsmetricTopic() {
		return dsmetricTopic;
	}

	public void setDsmetricTopic(String dsmetricTopic) {
		this.dsmetricTopic = dsmetricTopic;
	}

	public String getKafkaAddr() {
		return kafkaAddr;
	}

	public void setKafkaAddr(String kafkaAddr) {
		this.kafkaAddr = kafkaAddr;
	}

}
