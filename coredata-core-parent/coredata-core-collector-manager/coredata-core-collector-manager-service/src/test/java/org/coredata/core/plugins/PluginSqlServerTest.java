package org.coredata.core.plugins;

import static org.junit.Assert.assertTrue;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("sqlserver_test")
public class PluginSqlServerTest {

	@Autowired
	private DataSourceService dataSourceService;

	@Value(value = "${spring.datasource.url}")
	private String sqlserverUrl;

	@Value(value = "${spring.datasource.username}")
	private String sqlserverUserName;

	@Value(value = "${spring.datasource.password}")
	private String sqlserverPassword;

	@Before
	public void init() {
	}

	@After
	public void cleanup() throws IOException {

	}

	@Test
	public void sqlserverImportTest() throws InterruptedException, ExecutionException {

		PluginConfig readerConfig = new PluginConfig();
		readerConfig.put("url", sqlserverUrl);
		readerConfig.put("driver", "net.sourceforge.jtds.jdbc.Driver");
		readerConfig.put("table", "inst_pojo");
		readerConfig.put("username", sqlserverUserName);
		readerConfig.put("password", sqlserverPassword);
		readerConfig.put("parallelism", 1);
		readerConfig.put("max.size.per.fetch", 5);
		//record.limit 数据量大,只取100条,该参数只在数据预览时启用
		readerConfig.put("record.limit", 100);
		readerConfig.put("checkColumn", "ID");
		readerConfig.put("checkIndex", 0);

		JobConfig jobConfig = new DefaultJobConfig("jdbcReader", readerConfig, "debugWriter", new PluginConfig());
		Future<JobDetail> future = dataSourceService.run(jobConfig);
		JobDetail jobDetail = future.get();
		assertTrue(100 == jobDetail.getReaderCount());
		assertTrue(100 == jobDetail.getWriterCount());
	}

}
