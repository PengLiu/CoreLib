package org.coredata.core.metric;

import org.coredata.core.TestApp;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.repositories.MetricResp;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.metric.vos.MetricVal;
import org.coredata.util.query.TimeRange;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class MetricServiceTest {

	private long timebase = System.currentTimeMillis();

	@Autowired
	private MetricService metricService;

	@Autowired
	private MetricResp metricResp;

	@Before
	public void init() throws InterruptedException {

		List<Metric> metrics = new ArrayList<>();

		Metric metric = new Metric();
		metric.setMetricId("CPURate");
		metric.setEntityId("10L");
		metric.setStringVal("hello world");
		metric.setToken("1234567890");
		metric.setVal(1.0);
		metrics.add(metric);

		metric = new Metric();
		metric.setMetricId("MEMRate");
		metric.setEntityId("10L");
		metric.setVal(10.0);
		metric.setToken("1234567890");
		metrics.add(metric);

		metric = new Metric();
		metric.setMetricId("MEMRate");
		metric.setEntityId("11L");
		metric.setVal(10.0);
		metric.setToken("1234567890");
		metrics.add(metric);
		metricService.save(metrics);

		Thread.sleep(500);
		metric = new Metric();
		metric.setMetricId("MEMRate");
		metric.setEntityId("10L");
		metric.setVal(11.0);
		metric.setToken("123");
		metricService.save(metric);

		List<Metric> metricList = demoData("20L", "CPURate", "9234567890", 3);

		metricService.save(metricList);

		metricList = demoData("20L", "MEMRate", "9234567890", 3);
		metricService.save(metricList);

		metricList = demoData("21L", "CPURate", "9234567890", 3);
		metricService.save(metricList);

		metricList = demoData("21L", "MEMRate", "9234567890", 3);
		metricService.save(metricList);

		Page<Metric> page = metricService.findLastMetricVal("MEMRate", PageRequest.of(0, 1, Direction.DESC, "createdTime"));
		assertEquals(1, page.getContent().size());
		assertEquals("9234567890", page.getContent().get(0).getToken());

		page = metricService.findLastMetricVal("MEMRate", PageRequest.of(0, 1, Direction.ASC, "createdTime"));
		assertEquals(1, page.getContent().size());
		assertEquals("9234567890", page.getContent().get(0).getToken());

	}

	private List<Metric> demoData(String entityId, String metricId, String token, int size) {
		String _entityId = StringUtils.isEmpty(entityId) ? "10L" : entityId;
		String _metricId = StringUtils.isEmpty(metricId) ? "CPURate" : metricId;
		String _token = StringUtils.isEmpty(token) ? "1234567890" : token;

		List<Metric> metrics = new ArrayList<>();

		for (int i = 0; i < size; i++) {
			Metric metric = new Metric();
			metric.setMetricId(_metricId);
			metric.setEntityId(_entityId);
			metric.setStringVal("hello world" + i);
			metric.setToken(_token);
			metric.setVal((i + 1) * 10.0);
			metric.setCreatedTime(timebase + 1000 * 10 * i);
			metrics.add(metric);
		}

		return metrics;
	}

	@After
	public void cleanup() {
		metricResp.deleteAll();
	}

	@Test
    public void metricTest() {

		Map<String, Collection<Metric>> metrics = metricService.findLastMetricsByEntities(new String[] { "10L" });
		assertNotNull(metrics);
		assertEquals(2, metrics.entrySet().iterator().next().getValue().size());

		TimeRange timeRange = new TimeRange(System.currentTimeMillis() - 300 * 1000, System.currentTimeMillis(), DateHistogramInterval.seconds(30));

		Collection<MetricVal> metricVals = metricService.loadMetricByEntityAndTimeRange("10L", "MEMRate", timeRange, new String[] { "avg", "max", "min" });
		assertEquals(11, metricVals.size());
		MetricVal metricVal = ((List<MetricVal>) metricVals).get(10);
		assertTrue(11 == metricVal.getMax());
		assertTrue(10 == metricVal.getMin());
		assertTrue(10.5 == metricVal.getAvg());
		Map<String, Collection<Metric>> tmp = metricService.findLastMetricsByEntities(new String[] { "10L", "11L" });
		assertTrue(2 == tmp.size());
		Metric metric = tmp.get("11L").iterator().next();
		assertTrue(10 == metric.getVal());
		assertEquals("MEMRate", metric.getMetricId());
		assertTrue(2 == tmp.get("10L").size());

		Pageable pageable = PageRequest.of(0, 10, Direction.ASC, "createdTime");
		timeRange = new TimeRange(System.currentTimeMillis() - 300 * 1000, System.currentTimeMillis(), null, null);
		Page<Metric> pages = metricService.loadMetricByEntityAndTimeRange("10L", "MEMRate", timeRange, pageable);
		assertTrue(10 == pages.getContent().iterator().next().getVal());

		pageable = PageRequest.of(0, 10, Direction.DESC, "createdTime");
		pages = metricService.loadMetricByEntityAndTimeRange("10L", "MEMRate", timeRange, pageable);
		assertTrue(11 == pages.getContent().iterator().next().getVal());

	}

	@Test
    public void loadMetricsByEntityAndTimeRange() {
		TimeRange timeRange = new TimeRange(timebase, timebase + 40 * 1000, DateHistogramInterval.seconds(10));
		System.out.println(timebase + "----------------" + (timebase + 40 * 1000));
		Map<String, Collection<MetricVal>> metrics = metricService.loadMetricsByEntityAndTimeRange("20L", new String[] { "MEMRate", "CPURate" }, timeRange,
				new String[] { "avg" });
		assertNotNull(metrics);

	}

	@Test
	public void loadRawMetricsByEntityAndTimeRange() {
		TimeRange timeRange = new TimeRange(timebase - 60 * 1000, System.currentTimeMillis(), DateHistogramInterval.seconds(10));
		Map<String, Collection<Metric>> metrics = metricService.loadRawMetricsByEntityAndTimeRange("20L", new String[] { "MEMRate", "CPURate" }, timeRange);
		assertNotNull(metrics);

	}

	@Test
    public void loadMaxMetricByEntitiesAndTimeRange(){
        TimeRange timeRange = new TimeRange(timebase - 60 * 1000, System.currentTimeMillis(), DateHistogramInterval.seconds(10));
        Map<String, Object> metrics = metricService.loadMaxMetricByEntitiesAndTimeRange(new String[] {"20L","21L","10L"},  "MEMRate", timeRange);
        assertNotNull(metrics);
    }

    @Test
    public void findLastByEntitiesAndMetric(){
        List<Metric> metrics = metricService.findLastByEntitiesAndMetric(new String[] {"20L","21L","10L"},  "MEMRate");
        assertNotNull(metrics);
    }

    @Test
    public void loadMetricByEntitiesAndTimeRange(){

        TimeRange timeRange = new TimeRange(timebase - 60 * 1000, timebase, DateHistogramInterval.seconds(60));
        Map<String, Collection<MetricVal>> metrics = metricService.loadMetricByEntitiesAndTimeRange(new String[] {"20L","21L","10L"},  "MEMRate",timeRange,new String[] { "avg", "max", "min" });
        assertNotNull(metrics);
    }

}