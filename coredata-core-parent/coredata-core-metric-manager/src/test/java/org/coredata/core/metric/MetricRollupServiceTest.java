package org.coredata.core.metric;

import org.coredata.core.ElasticsearchService;
import org.coredata.core.TestApp;
import org.coredata.core.metric.repositories.MetricResp;
import org.coredata.core.metric.repositories.MetricRollupResp;
import org.coredata.core.metric.services.MetricRollupService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class MetricRollupServiceTest {

	@Autowired
	private MetricRollupService metricRollupService;

	@Autowired
	private MetricResp metricResp;

	@Autowired
	private MetricRollupResp metricRollupResp;
	
	@Autowired
	private ElasticsearchService elasticsearchService;

	@After
	public void cleanup() {
		metricResp.deleteAll();
		metricRollupResp.deleteAll();
	}

	@Test
	public void rollUp() {		
		metricRollupService.rollUp();
		elasticsearchService.forceMerge();
	}

}
