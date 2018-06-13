package org.coredata.core.util;

import org.coredata.core.util.querydsl.DSLBuilder;
import org.coredata.core.util.querydsl.Direction;
import org.coredata.core.util.querydsl.FilterGroupBuilder;
import org.coredata.core.util.querydsl.LogicOps;
import org.coredata.core.util.querydsl.QueryOps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;



@RunWith(SpringRunner.class)
public class DSLBuilderTest {

	@Test
	public void dslBuilderTest() {

		String result = DSLBuilder.instance().pageable(1, 10).sort(Direction.ASC, "field").filter(LogicOps.and)
				.filterGroup(FilterGroupBuilder.instance().filterGroup(LogicOps.and).filter(QueryOps.eq, "age", 20).filter(QueryOps.gte, "age", "50"))
				.filterGroup(FilterGroupBuilder.instance().filterGroup(LogicOps.or).filter(QueryOps.eq, "age", 20).filter(QueryOps.gte, "age", "50")).build();
		Assert.assertEquals(
				"{\"pagination\":{\"size\":10,\"page\":1},\"sort\":{\"direction\":\"ASC\",\"fields\":[\"field\"]},\"filter\":{\"and\":[{\"field\":\"age\",\"ops\":\"eq\",\"value\":20},{\"field\":\"age\",\"ops\":\"gte\",\"value\":\"50\"}],\"or\":[{\"field\":\"age\",\"ops\":\"eq\",\"value\":20},{\"field\":\"age\",\"ops\":\"gte\",\"value\":\"50\"}]}}",result);

	}

}
