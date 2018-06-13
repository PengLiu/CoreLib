package org.coredata.core.stream.output;

import java.util.List;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.coredata.core.metric.documents.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import akka.stream.Attributes;
import akka.stream.Inlet;
import akka.stream.SinkShape;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class SaveSourceDataSink<A> extends GraphStage<SinkShape<A>> {

	private static Logger logger = LoggerFactory.getLogger(SaveSourceDataSink.class);

	public final Inlet<A> in = Inlet.create("BigDataSink.in");

	private final SinkShape<A> shape = SinkShape.of(in);

	private KafkaProducer<String, String> producer;

	private String topic;

	public SaveSourceDataSink(String kafkaAddr, String topic, ActorSystem system, int parallelNum) {
		ProducerSettings<String, String> producerSettings = ProducerSettings.create(system, new StringSerializer(), new StringSerializer())
				.withBootstrapServers(kafkaAddr).withParallelism(parallelNum);
		this.producer = producerSettings.createKafkaProducer();
		this.topic = topic;
	}

	@Override
	public SinkShape<A> shape() {
		return shape;
	}

	@Override
	public GraphStageLogic createLogic(Attributes attr) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {
					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						List<Metric> metric = (List<Metric>) grab(in);
						try {
							for (Metric m : metric)
								producer.send(new ProducerRecord<String, String>(topic, UUID.randomUUID().toString(), JSON.toJSONString(m)));
						} catch (Exception e) {
							logger.error("Save metrics error.", e);
						} finally {
							pull(in);
						}
					}
				});
			}

			@Override
			public void preStart() {
				pull(in);
			}
		};
	}

}
