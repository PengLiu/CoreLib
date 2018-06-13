package org.coredata.core.metric;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.coredata.core.TestApp;
import org.coredata.core.metric.documents.LogFile;
import org.coredata.core.metric.repositories.LogResp;
import org.coredata.core.metric.services.LogService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@Ignore
public class LogServiceTest {

	@Autowired
	private LogService logService; 

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private LogResp logResp;

	private String log;

	@Before
	public void init() throws IOException {
		elasticsearchTemplate.createIndex(LogFile.class);
		log = IOUtils.toString(LogServiceTest.class.getResourceAsStream("/log.json"), StandardCharsets.UTF_8.name());
	}

	@After
	public void cleanup() {
		logResp.deleteAll();
	}

	@Test
	public void saveLogsTest() throws InterruptedException {
		kafkaTemplate.send("logs", UUID.randomUUID().toString(), log);
		Thread.sleep(10000);
		Assert.assertTrue(logResp.count() > 0); 
	}

}
