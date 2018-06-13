package org.coredata.core.stream;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.coredata.core.stream.actor.DeadMessage;
import org.coredata.core.stream.config.StreamConfig;
import org.coredata.core.stream.config.StreamFactory;
import org.coredata.core.stream.transform.functions.AbsFunction;
import org.coredata.core.stream.util.LookupTool;
import org.coredata.core.stream.vo.GraphVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.kafka.ConsumerMessage.CommittableMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.kafka.javadsl.Consumer.Control;
import akka.stream.ActorMaterializer;
import akka.stream.ActorMaterializerSettings;
import akka.stream.Materializer;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Source;

/**
 * 整个清洗，挖掘，决策流程的起始节点
 * 调整整个清洗，挖掘，告警，存储流程，通过akka的stream方式进行修改
 * 除去了不必要的kafkatopic，简化kafka的使用
 *
 * 此次改造想法如下：
 * 1.首先将不同partition作为单独的source进行流程图构建。
 * 2.未来如果有多个partition也可以自动创建多个可运行的流程图，只需要将流程run即可。
 * 3.将整个流程采用并行串行结合的方式进行改造，将flow拆分，每个flow做多个并行节点。
 * 4.并行节点数可配置
 * @author sue
 *
 */
@Component
public class DataStreamStarter {

	private static final Logger logger = LoggerFactory.getLogger(DataStreamStarter.class);

	@Autowired
	private StreamConfig config;

	@Autowired
	private LookupTool lookupTool;

	@Autowired
	private StreamGraph graph;

	@Autowired
	private StreamFactory streamFactory;

	private Materializer materializer;

	@SuppressWarnings("rawtypes")
	private RunnableGraph runnableGraph;

	Source<CommittableMessage<String, String>, Control> source;

	@PostConstruct
	public void init() {
		//init相关清洗函数
		AbsFunction.initFunction(lookupTool);
		this.materializer = ActorMaterializer.create(ActorMaterializerSettings.create(streamFactory.getSystem()).withInputBuffer(64, 64),
				streamFactory.getSystem());
		initSource();
		runnableGraph = spliceGraph();
		ActorRef deadLettersSubscriber = streamFactory.getSystem().actorOf(Props.create(DeadMessage.class, runnableGraph, materializer),
				"dead-letters-subscriber");
		streamFactory.getSystem().eventStream().subscribe(deadLettersSubscriber, DeadLetter.class);
		for (int i = 0; i < config.getRunTime(); i++) {
			runnableGraph.run(materializer);
		}
	}

	private void initSource() {
		String topic = config.getKafkaCoreDataTopic();
		if (logger.isDebugEnabled())
			logger.debug("Init transform kafka addr:" + streamFactory.getKafkaAddr() + " and read topic is:" + topic);
		ConsumerSettings<String, String> consumerSettings = ConsumerSettings
				.create(streamFactory.getSystem(), new StringDeserializer(), new StringDeserializer()).withBootstrapServers(streamFactory.getKafkaAddr())
				.withGroupId(topic + "_group").withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
				.withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "3000").withProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
				.withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		this.source = Consumer.committableSource(consumerSettings, Subscriptions.topics(topic));
	}

	/**
	 * 用于拼接流程图，返回自定义流程图类
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private RunnableGraph spliceGraph() {
		
		//配置数据源
		graph.setSource(this.source);
		//制作相关流程，清洗流程
		graph.setTransformFlows(streamFactory.getTransformModelFlow(), streamFactory.getFilterFlow());
		//挖掘流程
		graph.setMiningFlows(streamFactory.getMiningModelFlow(), streamFactory.getPrepareFlow(), streamFactory.getMiningFlow());
		graph.setSaveMetricSink(streamFactory.getSaveMetricSink());
		//决策流程
		graph.setDecisionModelFlow(streamFactory.getDecisionModelFlow());
		graph.setDecisionSink(streamFactory.getDecisionSink());
		GraphVO vo = new GraphVO(config.getParallelNum(), config.isSaveTransform(), config.isSaveMetric(), config.getDsTransformTopic(),
				config.getDsMetricTopic(), streamFactory.getKafkaAddr());
		return graph.spliceGraph(vo, streamFactory.getSystem());
	}

}
