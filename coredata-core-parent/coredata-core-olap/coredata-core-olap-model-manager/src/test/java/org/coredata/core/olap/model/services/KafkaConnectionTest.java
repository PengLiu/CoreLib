package org.coredata.core.olap.model.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.coredata.core.TestApp;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@Ignore
public class KafkaConnectionTest {

	@Value("${spring.kafka.bootstrap-servers}")
	private String kafkaAddr;

	@Value("${spring.kafka.consumer.group-id}")
	private String groupId;

	@Value("${spring.kafka.consumer.auto-offset-reset}")
	private String offsetRest;

	@Value("${spring.kafka.consumer.enable-auto-commit}")
	private String autoCommit;

	@Value("${spring.kafka.topics.olap}")
	private String topic;

	private int parallelism = 1;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private ActorSystem actorSystem;

	private ExecutorService executor = Executors.newFixedThreadPool(8);

	private CompletionStage<Done> say(String str) {
		System.err.println(str);
		return CompletableFuture.completedFuture(Done.getInstance());
	}

	@Test
	public void sendTest() throws InterruptedException {

		Materializer materializer = ActorMaterializer.create(actorSystem);

		ConsumerSettings<String, String> consumerSettings = ConsumerSettings.create(actorSystem, new StringDeserializer(), new StringDeserializer())
				.withBootstrapServers(kafkaAddr).withGroupId(groupId).withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit)
				.withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "3000").withProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000")
				.withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetRest);

		Consumer.committableSource(consumerSettings, Subscriptions.topics("test")).mapAsyncUnordered(parallelism, msg -> say(msg.record().value()))
				.runWith(Sink.ignore(), materializer);

		for (int i = 0; i < 8; i++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 100000; i++) {
						kafkaTemplate.send("test", Thread.currentThread().getName() + " hello world" + i);
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							;
						}
					}
				}
			});
		}
		
		Thread.sleep(300000);

	}

}
