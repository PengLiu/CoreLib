package org.coredata.core.stream;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.coredata.core.stream.actor.DeadMessage;
import org.coredata.core.stream.config.StreamConfig;
import org.coredata.core.stream.config.StreamFactory;
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
 * 告警中心流程图启动类
 * @author sue
 *
 */
@Component
public class AlarmStreamStarter {

	private static final Logger logger = LoggerFactory.getLogger(AlarmStreamStarter.class);

	@Autowired
	private StreamConfig config;

	@Autowired
	private AlarmStreamGraph alarmGraph;

	@Autowired
	private StreamFactory streamFactory;

	private Materializer materializer;

	@SuppressWarnings("rawtypes")
	private RunnableGraph runnableGraph;

	Source<CommittableMessage<String, String>, Control> source;

	@PostConstruct
	public void init() {
		this.materializer = ActorMaterializer.create(ActorMaterializerSettings.create(streamFactory.getSystem()).withInputBuffer(64, 64),
				streamFactory.getSystem());
		initSource();
		runnableGraph = spliceGraph();
		ActorRef deadLettersSubscriber = streamFactory.getSystem().actorOf(Props.create(DeadMessage.class, runnableGraph, materializer),
				"dead-letters-subscriber-alarm");
		streamFactory.getSystem().eventStream().subscribe(deadLettersSubscriber, DeadLetter.class);
		for (int i = 0; i < config.getRunTime(); i++) {
			runnableGraph.run(materializer);
		}
	}

	private void initSource() {
		String topic = config.getAlarmTopic();
		if (logger.isDebugEnabled())
			logger.debug("Init alarm center kafka addr:" + streamFactory.getKafkaAddr() + " and read topic is:" + topic);
		ConsumerSettings<String, String> consumerSettings = ConsumerSettings
				.create(streamFactory.getSystem(), new StringDeserializer(), new StringDeserializer()).withBootstrapServers(streamFactory.getKafkaAddr())
				.withGroupId(topic + "_group").withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
				.withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "3000").withProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
				.withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		this.source = Consumer.committableSource(consumerSettings, Subscriptions.topics(topic));
	}

	@SuppressWarnings("rawtypes")
	private RunnableGraph spliceGraph() {
		//配置数据源
		alarmGraph.setSource(this.source);
		//制作相关流程，告警流程
		alarmGraph.setProcessAlarmSink(streamFactory.getProcessAlarmSink());
		return alarmGraph.spliceGraph(config.getParallelNum());
	}

}
