package org.coredata.core.test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamConfig {
	
	/**
	 * 采集数据读取的topic
	 */
	@Value("${kafka.coredataTopic}")
	private String kafkaCoreDataTopic = null;

	/**
	 * 清洗后数据写入HDFS的topic
	 */
	@Value("${kafka.dstransformTopic}")
	private String dsTransformTopic = null;

	/**
	 * 挖掘后数据写入HDFS的topic
	 */
	@Value("${kafka.dsmetricTopic}")
	private String dsMetricTopic = null;

	/**
	 * redis订阅清洗模型主题
	 */
	@Value("${spring.redis.sub.transform}")
	private String subTransform = null;

	/**
	 * redis订阅挖掘模型主题
	 */
	@Value("${spring.redis.sub.mining}")
	private String subMining = null;

	/**
	 * redis订阅决策模型主题
	 */
	@Value("${spring.redis.sub.decision}")
	private String subDecision = null;

	/**
	 * 业务告警的kafka主题
	 */
	@Value("${kafka.metricTopic}")
	private String kafkaMetricTopic = null;

	/**
	 * 告警内容发送的kafka主题
	 */
	@Value("${kafka.alarmTopic}")
	private String alarmTopic = null;

	/**
	 * 流程并行数量
	 */
	@Value("${stream.parallelNum}")
	private int parallelNum = 2;

	/**
	 * 流程图run次数，默认1
	 */
	@Value("${stream.runtime: 1}")
	private int runTime = 1;

	/**
	 * 是否开启清洗数据写入HDFS，默认否
	 */
	@Value("${stream.saveTransform: false}")
	private boolean saveTransform = false;

	/**
	 * 是否开启挖掘后数据写入HDFS，默认否
	 */
	@Value("${stream.saveMetric: false}")
	private boolean saveMetric = false;

	/**
	 * actor部署方式，默认本地部署
	 */
	@Value("${cluster.type:Local}")
	private String clusterType = null;

	/**
	 * kafka连接方式
	 */
	@Value("${kafka.addrType}")
	private String kafkaType;

	/**
	 * zookeeper连接地址
	 */
	@Value("${zookeeper.addr}")
	private String zkAddr;

	public String getKafkaCoreDataTopic() {
		return kafkaCoreDataTopic;
	}

	public void setKafkaCoreDataTopic(String kafkaCoreDataTopic) {
		this.kafkaCoreDataTopic = kafkaCoreDataTopic;
	}

	public String getDsTransformTopic() {
		return dsTransformTopic;
	}

	public void setDsTransformTopic(String dsTransformTopic) {
		this.dsTransformTopic = dsTransformTopic;
	}

	public String getDsMetricTopic() {
		return dsMetricTopic;
	}

	public void setDsMetricTopic(String dsMetricTopic) {
		this.dsMetricTopic = dsMetricTopic;
	}

	public String getSubTransform() {
		return subTransform;
	}

	public void setSubTransform(String subTransform) {
		this.subTransform = subTransform;
	}

	public String getSubMining() {
		return subMining;
	}

	public void setSubMining(String subMining) {
		this.subMining = subMining;
	}

	public String getSubDecision() {
		return subDecision;
	}

	public void setSubDecision(String subDecision) {
		this.subDecision = subDecision;
	}

	public String getKafkaMetricTopic() {
		return kafkaMetricTopic;
	}

	public void setKafkaMetricTopic(String kafkaMetricTopic) {
		this.kafkaMetricTopic = kafkaMetricTopic;
	}

	public String getAlarmTopic() {
		return alarmTopic;
	}

	public void setAlarmTopic(String alarmTopic) {
		this.alarmTopic = alarmTopic;
	}

	public int getParallelNum() {
		return parallelNum;
	}

	public void setParallelNum(int parallelNum) {
		this.parallelNum = parallelNum;
	}

	public int getRunTime() {
		return runTime;
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
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

	public String getClusterType() {
		return clusterType;
	}

	public void setClusterType(String clusterType) {
		this.clusterType = clusterType;
	}

	public String getKafkaType() {
		return kafkaType;
	}

	public void setKafkaType(String kafkaType) {
		this.kafkaType = kafkaType;
	}

	public String getZkAddr() {
		return zkAddr;
	}

	public void setZkAddr(String zkAddr) {
		this.zkAddr = zkAddr;
	}

}
