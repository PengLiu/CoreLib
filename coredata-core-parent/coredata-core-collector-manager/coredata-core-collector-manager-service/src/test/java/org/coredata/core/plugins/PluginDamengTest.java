package org.coredata.core.plugins;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.coredata.core.TestApp;
import org.coredata.core.data.DefaultJobConfig;
import org.coredata.core.data.JobConfig;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.service.DataSourceService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Transactional
@EnableAsync
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("dameng_test")
public class PluginDamengTest {

	@Autowired
	private DataSourceService dataSourceService;

	@Value(value = "${spring.datasource.url}")
	private String damengUrl;

	@Value(value = "${spring.datasource.username}")
	private String damengUserName;

	@Value(value = "${spring.datasource.password}")
	private String damengPassword;
	
	@Value(value = "${spring.datasource.driverClassName}")
	private String driverClassName;
	

	@Before
	public void init() {

	}

	@After
	public void cleanup() throws IOException {
	}

	@Test
	public void damengImportTest() throws InterruptedException, ExecutionException {

		PluginConfig readerConfig = new PluginConfig();
		readerConfig.put("url", damengUrl);
		readerConfig.put("driver", driverClassName);
		readerConfig.put("table", "test_table");
		readerConfig.put("username", damengUserName);
		readerConfig.put("password", damengPassword);
		readerConfig.put("parallelism", 1);
		readerConfig.put("max.size.per.fetch", 5);
		readerConfig.put("checkColumn", "created_time");
		readerConfig.put("checkIndex", 2);

		JobConfig jobConfig = new DefaultJobConfig("jdbcReader", readerConfig, "debugWriter", new PluginConfig());
		Future<JobDetail> future = dataSourceService.run(jobConfig);
		JobDetail jobDetail = future.get();
		Assert.assertTrue(jobDetail.getReaderCount() > 0);
		Assert.assertTrue(jobDetail.getWriterCount() > 0);

	}

}
