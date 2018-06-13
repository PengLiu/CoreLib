package org.coredata.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.coredata.core.TestApp;
import org.coredata.core.data.Constants;
import org.coredata.core.data.DefaultJobConfig;
import org.coredata.core.data.JobConfig;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.service.DataSourceService;
import org.coredata.core.data.writers.elasticsearch.ElasticSearchProperties;
import org.coredata.core.entities.repositories.EntityResp;
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
public class PluginCsvTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private DataSourceService dataSourceService;

	@Autowired
	private EntityResp entityResp;

	@Value(value = "${spring.data.elasticsearch.cluster-nodes}")
	private String esAddresses;

	private String path;

	private String esUrl;

	@Before
	public void init() {
		File file = new File(PluginCsvTest.class.getResource("/application.yml").getFile());
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

		entityResp.deleteAll();
	}

	@Test
	public void csvImportTest() throws InterruptedException, ExecutionException, IOException {

		PluginConfig readerConfig = new PluginConfig();
		readerConfig.put("path", path + File.separator + "data.csv");
		readerConfig.put("encoding", "utf-8");
		readerConfig.put("recordSeparator", "\r\n");
		readerConfig.put("delimiter", ",");
		readerConfig.put("start.row", 1);

		JobConfig jobConfig = new DefaultJobConfig("csvReader", readerConfig, "debugWriter", new PluginConfig());
		Future<JobDetail> future = dataSourceService.run(jobConfig);
		JobDetail jobDetail = future.get();
		Assert.assertTrue(70 == jobDetail.getReaderCount());
		Assert.assertTrue(70 == jobDetail.getWriterCount());

		Assert.assertEquals("station_id", jobDetail.getRecords().get(0).get(0));
		Assert.assertEquals("83", jobDetail.getLastVal().get(0));

		readerConfig.put("start.row", 2);
		jobConfig = new DefaultJobConfig("csvReader", readerConfig, "debugWriter", new PluginConfig());
		future = dataSourceService.run(jobConfig);
		jobDetail = future.get();
		Assert.assertTrue(69 == jobDetail.getReaderCount());
		Assert.assertTrue(69 == jobDetail.getWriterCount());
		Assert.assertEquals("2", jobDetail.getRecords().get(0).get(0));
		Assert.assertEquals("83", jobDetail.getLastVal().get(0));

		JsonNode propertyDef = mapper.readTree(PluginCsvTest.class.getResourceAsStream("/data_property.json"));

		PluginConfig writerConfig = new PluginConfig();
		writerConfig.put("index.name", "testcsv");
		writerConfig.put("index.template", mapper.writeValueAsString(propertyDef));
		writerConfig.put("cluster.address", esUrl);

		JsonNode dateFilter = mapper.readTree(PluginCsvTest.class.getResource("/date_filter.json"));

		String script = IOUtils.toString(PluginCsvTest.class.getResourceAsStream("/entity_generator.js"));

		readerConfig.put("start.row", 2);
		readerConfig.put(Constants.FILTER_CONFIG, mapper.writeValueAsString(dateFilter));
		writerConfig.put(ElasticSearchProperties.ENTITY_GENERATOR_SCRIPT, script);

		jobConfig = new DefaultJobConfig("csvReader", readerConfig, "esWriter", writerConfig, null, "123456");
		future = dataSourceService.run(jobConfig);
		jobDetail = future.get();
		Thread.sleep(10000);
		Assert.assertEquals(69, jobDetail.getReaderCount());
		Assert.assertEquals(69, jobDetail.getWriterCount());
	}

}
