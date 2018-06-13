package org.coredata.core.datastream.blueprint;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.coredata.core.TestApp;
import org.coredata.core.datastream.blueprint.actor.BluePrintActor;
import org.coredata.core.datastream.blueprint.service.BluePrintService;
import org.coredata.core.datastream.blueprint.vo.BatchType;
import org.coredata.core.datastream.blueprint.vo.BluePrint;
import org.coredata.core.datastream.blueprint.vo.EntityFragment;
import org.coredata.core.entities.services.EntityService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

@EnableScheduling
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
//@Ignore
public class BluePrintTest {

	@Autowired
	private BluePrintService bluePrintService;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private EntityService entityService;

	@Value("${spring.kafka.topics.data_import}")
	private String topic;

	private ObjectMapper mapper = new ObjectMapper();

	private List<String> lines;

	@Autowired
	private ActorSystem actorSystem;

	@Before
	public void init() throws IOException {
		lines = IOUtils.readLines(BluePrintTest.class.getResourceAsStream("/data.json"), "UTF-8");
	}

	@After
	public void cleanup() {
		entityService.removeAllEntities();
	}

	@Test
	@Rollback(false)
	public void entityPerformanceTest() throws Exception {

		BluePrint bluePrint = new BluePrint();
		bluePrint.setToken("123456");

		String entityScript = IOUtils.toString(BluePrintTest.class.getResourceAsStream("/entity_batch_generator.js"));

		EntityFragment entityFragment = new EntityFragment();
		entityFragment.setJobId("job01");
		entityFragment.setScript(entityScript);

		bluePrint.addEntityFragment(entityFragment);

		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();

		Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(bluePrint.getRunner(), BluePrintActor.Cmd.ShowChildren, askTimeout);
		List<String> paths = (List<String>) Await.result(future, askTimeout.duration());

		ExecutorService executor = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					for (String line : lines) {
						try {
							ObjectNode data = mapper.createObjectNode();
							data.set("data", mapper.readTree(line));
							data.put("jobId", "job01");
							kafkaTemplate.send(topic, mapper.writeValueAsString(data));
						} catch (IOException e) {
							;
						}
					}
				}
			});
		}

		Thread.sleep(5000);

		long count = entityService.countEntityByCondition(null);
		Assert.assertEquals(700L, count);
		bluePrint.stop();
		for (String path : paths) {
			actorSystem.actorSelection(path).tell(BluePrintActor.Cmd.class, ActorRef.noSender());
		}
		Thread.sleep(5000);

	}

	@Test
	@Rollback(false)
	public void entityBatchGenGroupTimeTest() throws Exception {

		BluePrint bluePrint = new BluePrint();
		bluePrint.setToken("123456");

		String entityScript = IOUtils.toString(BluePrintTest.class.getResourceAsStream("/entity_batch_generator.js"));

		EntityFragment entityFragment = new EntityFragment();
		entityFragment.setBatchType(BatchType.Time);
		entityFragment.setTimeOut(2);
		entityFragment.setJobId("job01");
		entityFragment.setScript(entityScript);

		bluePrint.addEntityFragment(entityFragment);

		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();

		Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(bluePrint.getRunner(), BluePrintActor.Cmd.ShowChildren, askTimeout);
		List<String> paths = (List<String>) Await.result(future, askTimeout.duration());

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (String line : lines) {
					try {
						ObjectNode data = mapper.createObjectNode();
						data.set("data", mapper.readTree(line));
						data.put("jobId", "job01");
						kafkaTemplate.send(topic, mapper.writeValueAsString(data));
					} catch (IOException e) {
						;
					}
				}
			}
		});
		Thread.sleep(5000);
		long count = entityService.countEntityByCondition(null);
		Assert.assertEquals(70L, count);
		bluePrint.stop();
		for (String path : paths) {
			actorSystem.actorSelection(path).tell(BluePrintActor.Cmd.class, ActorRef.noSender());
		}
		Thread.sleep(5000);

	}

	@Test
	@Rollback(false)
	public void entityBatchGenGroupWithInTest() throws Exception {

		BluePrint bluePrint = new BluePrint();
		bluePrint.setToken("123456");

		String entityScript = IOUtils.toString(BluePrintTest.class.getResourceAsStream("/entity_batch_generator.js"));

		EntityFragment entityFragment = new EntityFragment();
		entityFragment.setBatchType(BatchType.GroupWithIn);
		entityFragment.setBatchSize(9);
		entityFragment.setTimeOut(2);
		entityFragment.setJobId("job01");
		entityFragment.setScript(entityScript);

		bluePrint.addEntityFragment(entityFragment);

		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();

		Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(bluePrint.getRunner(), BluePrintActor.Cmd.ShowChildren, askTimeout);
		List<String> paths = (List<String>) Await.result(future, askTimeout.duration());

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (String line : lines) {
					try {
						ObjectNode data = mapper.createObjectNode();
						data.set("data", mapper.readTree(line));
						data.put("jobId", "job01");
						kafkaTemplate.send(topic, mapper.writeValueAsString(data));
					} catch (IOException e) {
						;
					}
				}
			}
		});
		Thread.sleep(5000);
		long count = entityService.countEntityByCondition(null);
		Assert.assertEquals(70L, count);
		bluePrint.stop();
		for (String path : paths) {
			actorSystem.actorSelection(path).tell(BluePrintActor.Cmd.class, ActorRef.noSender());
		}
		Thread.sleep(5000);
	}

	@Test
	@Rollback(false)
	public void entityBatchGenNoGroupedTest() throws Exception {

		BluePrint bluePrint = new BluePrint();
		bluePrint.setToken("123456");

		String entityScript = IOUtils.toString(BluePrintTest.class.getResourceAsStream("/entity_batch_generator.js"));

		EntityFragment entityFragment = new EntityFragment();
		entityFragment.setBatchType(BatchType.Group);
		entityFragment.setBatchSize(9);
		entityFragment.setJobId("job01");
		entityFragment.setScript(entityScript);

		bluePrint.addEntityFragment(entityFragment);

		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();

		Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(bluePrint.getRunner(), BluePrintActor.Cmd.ShowChildren, askTimeout);
		List<String> paths = (List<String>) Await.result(future, askTimeout.duration());

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (String line : lines) {
					try {
						ObjectNode data = mapper.createObjectNode();
						data.set("data", mapper.readTree(line));
						data.put("jobId", "job01");
						kafkaTemplate.send(topic, mapper.writeValueAsString(data));
					} catch (IOException e) {
						;
					}
				}
			}
		});
		Thread.sleep(5000);
		long count = entityService.countEntityByCondition(null);
		Assert.assertEquals(63L, count);
		bluePrint.stop();
		for (String path : paths) {
			actorSystem.actorSelection(path).tell(BluePrintActor.Cmd.class, ActorRef.noSender());
		}
		Thread.sleep(5000);
	}

	@Test
	@Rollback(false)
	public void entityBatchGenTest() throws Exception {

		BluePrint bluePrint = new BluePrint();
		bluePrint.setToken("123456");

		String entityScript = IOUtils.toString(BluePrintTest.class.getResourceAsStream("/entity_batch_generator.js"));

		EntityFragment entityFragment = new EntityFragment();
		entityFragment.setBatchType(BatchType.Group);
		entityFragment.setBatchSize(10);
		entityFragment.setJobId("job01");
		entityFragment.setScript(entityScript);

		bluePrint.addEntityFragment(entityFragment);

		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();

		Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(bluePrint.getRunner(), BluePrintActor.Cmd.ShowChildren, askTimeout);
		List<String> paths = (List<String>) Await.result(future, askTimeout.duration());

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (String line : lines) {
					try {
						ObjectNode data = mapper.createObjectNode();
						data.set("data", mapper.readTree(line));
						data.put("jobId", "job01");
						kafkaTemplate.send(topic, mapper.writeValueAsString(data));
					} catch (IOException e) {
						;
					}
				}
			}
		});
		Thread.sleep(5000);
		long count = entityService.countEntityByCondition(null);
		Assert.assertEquals(70L, count);
		bluePrint.stop();
		for (String path : paths) {
			actorSystem.actorSelection(path).tell(BluePrintActor.Cmd.class, ActorRef.noSender());
		}
		Thread.sleep(5000);
	}

	@Test
	@Rollback(false)
	public void entityGenTest() throws Exception {

		BluePrint bluePrint = new BluePrint();
		bluePrint.setToken("123456");

		String entityScript = IOUtils.toString(BluePrintTest.class.getResourceAsStream("/entity_generator.js"));

		EntityFragment entityFragment = new EntityFragment();
		entityFragment.setJobId("job01");
		entityFragment.setScript(entityScript);

		bluePrint.addEntityFragment(entityFragment);

		bluePrintService.buildGraph(bluePrint);
		bluePrint.run();

		Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(bluePrint.getRunner(), BluePrintActor.Cmd.ShowChildren, askTimeout);
		List<String> paths = (List<String>) Await.result(future, askTimeout.duration());

		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.submit(new Runnable() {
			@Override
			public void run() {
				for (String line : lines) {
					try {
						ObjectNode data = mapper.createObjectNode();
						data.set("data", mapper.readTree(line));
						data.put("jobId", "job01");
						kafkaTemplate.send(topic, mapper.writeValueAsString(data));
					} catch (IOException e) {
						;
					}
				}
			}
		});
		Thread.sleep(5000);
		long count = entityService.countEntityByCondition(null);
		Assert.assertEquals(70L, count);
		bluePrint.stop();

		for (String path : paths) {
			actorSystem.actorSelection(path).tell(BluePrintActor.Cmd.class, ActorRef.noSender());
		}
		Thread.sleep(5000);
	}

}