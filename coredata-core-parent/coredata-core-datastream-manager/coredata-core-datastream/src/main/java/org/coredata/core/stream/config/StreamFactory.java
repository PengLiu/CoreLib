package org.coredata.core.stream.config;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.stream.alarm.ProcessAlarmSink;
import org.coredata.core.stream.alarm.consumer.QueueManager;
import org.coredata.core.stream.decision.DecisionSink;
import org.coredata.core.stream.decision.SysDecisionModelFlow;
import org.coredata.core.stream.decision.TransformMetricFlow;
import org.coredata.core.stream.mining.MiningDataFlow;
import org.coredata.core.stream.mining.PrepareMiningDataFlow;
import org.coredata.core.stream.mining.SaveMetricSink;
import org.coredata.core.stream.mining.SysMiningModelFlow;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.service.AlarmCenterService;
import org.coredata.core.stream.service.MiningStreamService;
import org.coredata.core.stream.service.StreamService;
import org.coredata.core.stream.service.TransformStreamService;
import org.coredata.core.stream.transform.FilterFlow;
import org.coredata.core.stream.transform.SysTransformModelFlow;
import org.coredata.core.stream.util.KafkaUtils;
import org.coredata.core.util.actor.ActorCluster;
import org.coredata.core.util.actor.ClusterType;
import org.coredata.core.util.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;

@Component
public class StreamFactory {

	@Autowired
	private StreamConfig config;

	@Autowired
	private RedisService redisService;

	@Resource(type = TransformStreamService.class)
	private StreamService transformService;

	@Resource(type = MiningStreamService.class)
	private StreamService miningService;

	@Autowired
	private MetricService metricService;

	@Autowired
	private AlarmCenterService alarmCenterService;

	@Autowired
	private QueueManager queueManager;

	private ActorSystem system;

	private String kafkaAddr;

	private ActorCluster cluster = new ActorCluster();

	//同步清洗模型flow
	private SysTransformModelFlow<String, String> transformModelFlow;

	//清洗flow
	private FilterFlow<String, String> filterFlow;

	//同步挖掘模型flow
	private SysMiningModelFlow<String, String> miningModelFlow;

	//准备挖掘flow
	private PrepareMiningDataFlow<String, List<MiningData>> prepareFlow;

	//数据挖掘flow
	private MiningDataFlow<List<MiningData>, List<Metric>> miningFlow;

	//保存指标sink
	private Sink<List<Metric>, NotUsed> saveMetricSink;

	//同步决策模型flow
	private SysDecisionModelFlow<List<Metric>, List<Metric>> decisionModelFlow;

	//保存决策sink
	private Sink<List<Metric>, NotUsed> decisionSink;

	//清洗数据转换
	private TransformMetricFlow<String, List<Metric>> transformFlow;

	//处理告警sink
	private ProcessAlarmSink<String> processAlarmSink;

	@PostConstruct
	public void initFactory() {
		String clusterType = config.getClusterType();
		this.system = cluster.initActorSystem(ClusterType.valueOf(clusterType), null);
		this.kafkaAddr = KafkaUtils.getBrokers(config.getKafkaType(), config.getZkAddr());
		//制作相关流程
		this.transformModelFlow = new SysTransformModelFlow<>(redisService, config.getSubTransform());
		this.filterFlow = new FilterFlow<>(transformService);
		this.miningModelFlow = new SysMiningModelFlow<>(redisService, config.getSubMining());
		this.prepareFlow = new PrepareMiningDataFlow<>(miningService);
		this.miningFlow = new MiningDataFlow<>(redisService, miningService);
		this.saveMetricSink = Sink.fromGraph(new SaveMetricSink<List<Metric>>(metricService));
		//决策流程
		this.decisionModelFlow = new SysDecisionModelFlow<>(redisService, config.getSubDecision());
		this.decisionSink = Sink.fromGraph(new DecisionSink<>(this.kafkaAddr, this.system, config.getParallelNum(), config.getAlarmTopic(), metricService));
		this.transformFlow = new TransformMetricFlow<>();
		this.processAlarmSink = new ProcessAlarmSink<String>(queueManager, alarmCenterService, redisService);
	}

	public SysTransformModelFlow<String, String> getTransformModelFlow() {
		return transformModelFlow;
	}

	public FilterFlow<String, String> getFilterFlow() {
		return filterFlow;
	}

	public SysMiningModelFlow<String, String> getMiningModelFlow() {
		return miningModelFlow;
	}

	public PrepareMiningDataFlow<String, List<MiningData>> getPrepareFlow() {
		return prepareFlow;
	}

	public MiningDataFlow<List<MiningData>, List<Metric>> getMiningFlow() {
		return miningFlow;
	}

	public Sink<List<Metric>, NotUsed> getSaveMetricSink() {
		return saveMetricSink;
	}

	public SysDecisionModelFlow<List<Metric>, List<Metric>> getDecisionModelFlow() {
		return decisionModelFlow;
	}

	public Sink<List<Metric>, NotUsed> getDecisionSink() {
		return decisionSink;
	}

	public ActorSystem getSystem() {
		return system;
	}

	public String getKafkaAddr() {
		return kafkaAddr;
	}

	public TransformMetricFlow<String, List<Metric>> getTransformFlow() {
		return transformFlow;
	}

	public ProcessAlarmSink<String> getProcessAlarmSink() {
		return processAlarmSink;
	}

}
