package org.coredata.core.util;

import org.coredata.core.ElasticsearchService;
import org.coredata.core.TestApp;
import org.coredata.core.entities.Order;
import org.coredata.core.entities.TestData;
import org.coredata.core.repository.TestResp;
import org.coredata.core.util.elasticsearch.querydsl.AggregationBuilder;
import org.coredata.core.util.elasticsearch.vo.CommResult;
import org.coredata.core.util.querydsl.DSLBuilder;
import org.coredata.core.util.querydsl.QueryOps;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class QueryTest {

	@Autowired
	private TestResp testResp;

	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@After
	public void cleanup() {
		testResp.deleteAll();
	}

	@Before
	public void init() {
		elasticsearchTemplate.createIndex(TestData.class);
		for (int i = 0; i < 100; i++) {
			TestData test = new TestData();
			test.setUserId("user" + i);
			test.setResponseTime(i * 10);
			Order order = new Order();
			order.setLocation(i % 2 == 0 ? "beijing" : "tianjin");
			order.setPrice(i * 100);
			test.setOrder(order);
			testResp.save(test);
		}
	}

	@Test
	public void rangeAggTest() throws QuerydslException {

		AggregationBuilder builder = AggregationBuilder.instance().rangeAggregation("rangeAgg", "responseTime").range(1.0, 500.0).range(500.0, 1000.0).build();
		builder.subAggs(AggregationBuilder.instance().termAggregation("usrename", "userId"));
		String queryDsl = org.coredata.core.util.elasticsearch.querydsl.DSLBuilder.instance().aggregation(builder).pageable(1, 1).build();
		CommResult result = elasticsearchService.queryByCondition(queryDsl, "test");
		Assert.assertNotNull(result.getAggregations().get("rangeAgg"));

		builder = AggregationBuilder.instance().nestedAggregation("orderNestedAgg", "location", "order");
		builder.subAggs(AggregationBuilder.instance().termAggregation("location", "order.location"));
		queryDsl = org.coredata.core.util.elasticsearch.querydsl.DSLBuilder.instance().aggregation(builder).pageable(1, 1).build();
		result = elasticsearchService.queryByCondition(queryDsl, "test");
		Assert.assertNotNull(result.getAggregations().get("orderNestedAgg"));

		builder = AggregationBuilder.instance().distinctAggregation("distinct", "userId");
		queryDsl = org.coredata.core.util.elasticsearch.querydsl.DSLBuilder.instance().aggregation(builder).pageable(1, 1).build();
		result = elasticsearchService.queryByCondition(queryDsl, "test");
		Assert.assertEquals(100L, result.getAggregations().get("distinct").get("count").asLong());
	}

	@Test
	public void queryDslTest() throws QuerydslException {
		String query = DSLBuilder.instance().filter(QueryOps.eq, "userId", "user1").build();
		CommResult result = elasticsearchService.queryByCondition(query, "test");
		Assert.assertEquals(1L, result.getTotal());
		query = DSLBuilder.instance().build();
		result = elasticsearchService.queryByCondition(null, "test");
		Assert.assertEquals(100L, result.getTotal());
		query = DSLBuilder.instance().pageable(1, 5).build();
		result = elasticsearchService.queryByCondition(query, "test");
		Assert.assertEquals(100L, result.getTotal());
		Assert.assertEquals(5L, result.getRecords().size());
	}

}