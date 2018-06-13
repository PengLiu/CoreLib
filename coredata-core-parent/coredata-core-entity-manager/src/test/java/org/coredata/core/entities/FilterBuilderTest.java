package org.coredata.core.entities;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.coredata.core.entities.services.Neo4jFilterBuilder;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class FilterBuilderTest {

	private String filters;

	private String filtersS;

	@Before
	public void init() throws IOException {
		InputStream is = FilterBuilderTest.class.getResourceAsStream("/condition.json");
		filters = IOUtils.toString(is);

		is = FilterBuilderTest.class.getResourceAsStream("/condition_simple.json");
		filtersS = IOUtils.toString(is);
	}

	@Test
	public void filterBuilderTest() throws QuerydslException {

		Neo4jFilterBuilder builder = new Neo4jFilterBuilder(filters);
		Assert.assertEquals("((e.age <= 20 and e.name='user_name' ) or (e.age <= 20 and e.name='user_name2' )) and (e.age <= 50 ) or (e.age > 37 )",
				builder.buildFilters());

		builder = new Neo4jFilterBuilder(filtersS);
		Assert.assertEquals("e.age=~'.*001.*'", builder.buildFilters());

	}

}
