package org.coredata.core.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.TestApp;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class SaveMetricTest {

	@Autowired
	private MetricService metricService;

	private List<Metric> metricSource = new ArrayList<>();

	@Before
	public void init() {
		String path = TestApp.class.getClassLoader().getResource("source/mining_linux.txt").getPath();
		File file = new File(path);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = br.readLine()) != null)
				metricSource.add(JSON.parseObject(line, Metric.class));
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Test
	public void saveMetricTest() {
		for (Metric metric : metricSource) {
			Metric m = metricService.save(metric);
			assertNotEquals(null, m);
			assertEquals("b6fec5658d783a092423bf55dcfd44b3", m.getEntityId());
		}
	}

}
