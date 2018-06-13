package org.coredata.core.model.service;

import org.coredata.core.TestApp;
import org.coredata.core.model.common.Restype;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;

//@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class RestypeServiceTest {

	@Autowired
	private RestypeService service;

	@Before
	public void init() throws JsonProcessingException {

		Restype entity = new Restype();
		entity.setId("r1");
		entity.setCustomerId("1234567890");
		service.save(entity);
		entity.setId("r2");
		entity.setName("类型2");
		entity.setCustomerId("1234567890");
		service.save(entity);
	}
	@Test
	public void save() throws JsonProcessingException {

		Restype entity = new Restype();
		entity.setId("r3");
		entity.setParentid("r1");
		entity.setCustomerId("1234567890");
		service.save(entity);
		entity.setId("r4");
		entity.setName("类型4");
		entity.setParentid("r2");
		entity.setCustomerId("1234567890");
		service.save(entity);
		entity.setId("r6");
		entity.setName("类型6");
		entity.setCustomerId("1234567890");
		entity.setParentid("r4");
		service.save(entity);
	}

}
