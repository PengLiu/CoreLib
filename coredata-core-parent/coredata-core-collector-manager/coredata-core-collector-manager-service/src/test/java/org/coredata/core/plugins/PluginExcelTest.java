package org.coredata.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.coredata.core.TestApp;
import org.coredata.core.data.Constants;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@EnableAsync
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("mysql_test")
public class PluginExcelTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private DataSourceService dataSourceService;

	@Value(value = "${spring.data.elasticsearch.cluster-nodes}")
	private String esAddresses;

	private String path;

	private String esUrl;

	@Before
	public void init() {
		File file = new File(PluginExcelTest.class.getResource("/application.yml").getFile());
		path = file.getParent();
		String[] addrs = esAddresses.split(",");
		esUrl = addrs[0].split(":")[0] + ":9200";
	}

	@After
	public void cleanup() throws IOException {

		URL realUrl = new URL("http://" + esUrl + "/*");
		HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("DELETE");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("charset", "utf-8");
		connection.setUseCaches(false);
		connection.getResponseCode();
	}

	@Test
	public void excelImportTest() throws InterruptedException, ExecutionException, IOException {

		PluginConfig readerConfig = new PluginConfig();
		readerConfig.put("path", path + File.separator + "data.xlsx");
		readerConfig.put("start.row", 1);

		JobConfig jobConfig = new DefaultJobConfig("excelReader", readerConfig, "debugWriter", new PluginConfig());
		Future<JobDetail> future = dataSourceService.run(jobConfig);
		JobDetail jobDetail = future.get();
		Assert.assertTrue(70 == jobDetail.getReaderCount());
		Assert.assertTrue(70 == jobDetail.getWriterCount());

		Assert.assertEquals("station_id", jobDetail.getRecords().get(0).get(0));
		Assert.assertTrue(83 == ((Double)jobDetail.getLastVal().get(0)).intValue());

		readerConfig.put("start.row", 2);
		jobConfig = new DefaultJobConfig("excelReader", readerConfig, "debugWriter", new PluginConfig());
		future = dataSourceService.run(jobConfig);
		jobDetail = future.get();
		Assert.assertTrue(69 == jobDetail.getReaderCount());
		Assert.assertTrue(69 == jobDetail.getWriterCount());
		Assert.assertTrue(2 == ((Double)jobDetail.getRecords().get(0).get(0)).intValue());
		Assert.assertTrue(83 == ((Double)jobDetail.getLastVal().get(0)).intValue());

		JsonNode propertyDef = mapper.readTree(PluginExcelTest.class.getResourceAsStream("/data_property.json"));

		PluginConfig writerConfig = new PluginConfig();
		writerConfig.put("index.name", "testexcel");
		writerConfig.put("index.template", mapper.writeValueAsString(propertyDef));
		writerConfig.put("cluster.address", esUrl);

		JsonNode dateFilter = mapper.readTree(PluginExcelTest.class.getResource("/date_filter.json"));

		readerConfig.put("start.row", 2);
		readerConfig.put(Constants.FILTER_CONFIG, mapper.writeValueAsString(dateFilter));

		jobConfig = new DefaultJobConfig("excelReader", readerConfig, "esWriter", writerConfig);
		future = dataSourceService.run(jobConfig);
		jobDetail = future.get();		
		Thread.sleep(5000);		
		Assert.assertTrue(69 == jobDetail.getReaderCount());
		Assert.assertTrue(69 == jobDetail.getWriterCount());
	}

}
