package org.coredata.core.util;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.core.util.IOUtils;
import org.coredata.core.util.elasticsearch.querydsl.AggregationBuilder;
import org.coredata.core.util.elasticsearch.querydsl.DSLBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ESDslBuilderTest {

	@Test
	public void dslBuilderAggTest() throws IOException {

		String example = IOUtils.toString(new InputStreamReader(ESDslBuilderTest.class.getResourceAsStream("/agg.json")));

		String agg = DSLBuilder.instance().aggregation(AggregationBuilder.instance().termAggregation("termAgg", "name"))
				.aggregation(AggregationBuilder.instance().dateHistogramAggregation("dateAgg", "taskTime", "6s")
						.subAggs(AggregationBuilder.instance().ipRangeAggregation("subIp", "clientIp").build()))
				.aggregation(AggregationBuilder.instance().ipRangeAggregation("ipRange", "serverIpd").masks("172.16.1.0/24", "172.16.1.1/24").build()).build();
		Assert.assertEquals(example, agg);
		
		
	}

}
