package org.coredata.core.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.coredata.core.TestApp;
import org.coredata.core.entities.services.EntityService;
import org.coredata.core.util.querydsl.exception.QuerydslException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class EntityServiceTest {

	@Autowired
	private EntityService entityService;

	@Before
	public void init() throws JsonProcessingException {

		CommEntity entity = new CommEntity();
		entity.setEntityId("001");
		entity.setName("Entity 001");
		entity.setToken("1234567890");
		entity.setType("/os/windows/nic");
		entity.addProp("vendor", "oracle");
		entity.addProp("version", "1.0.x");
		entity.addProp("isAsset", "true");
		entity.addProp("vv", 10L);

		entityService.save(entity);

		ResEntity res = new ResEntity();
		res.setEntityId("001/001");
		res.setStatus("green");
		res.setName("Entity 002");
		res.setToken("1234567890");
		res.setType("/os/windows");
		res.addProp("vendor", "oracle");
		res.addProp("version", "1.0.x");
		res.addProp("val", 10L);
		res.addConn("ip", "127.0.0.1");
		res.addProp("isAsset", true);

		entityService.save(res);

		ResEntity res2 = new ResEntity();
		res2.setEntityId("001/002");
		res2.setStatus("red");
		res2.setName("Entity 003");
		res2.setToken("12345");
		res2.setType("/os/windows");
		res2.addProp("vendor", "oracle");
		res2.addProp("version", "1.0.x");
		res2.addConn("ip", "192.168.0.1");
		entityService.save(res2);

		entityService.createRelationship(entity.getEntityId(), res.getEntityId(), "test001", Direction.OUTGOING);
		Collection<CommEntity> entities = entityService.findByRelatedEntity(entity.getId(), "test001", 0, Direction.OUTGOING);
		assertEquals(1, entities.size());

		entityService.createRelationship(entity.getId(), res.getId(), "test", Direction.OUTGOING);
		entityService.createRelationship(res.getId(), res2.getId(), "test", Direction.OUTGOING);

		entities = entityService.findByRelatedEntity(entity.getId(), "test", 0, Direction.OUTGOING);
		assertEquals(1, entities.size());

		entities = entityService.findByRelatedEntity(entity.getId(), "test", 1, Direction.OUTGOING);
		assertEquals(1, entities.size());

		entities = entityService.findByRelatedEntity(entity.getId(), "test", 2, Direction.OUTGOING);
		assertEquals(2, entities.size());

		entities = entityService.findByRelatedEntity(entity.getId(), "test", 2, Direction.INCOMING);
		assertEquals(0, entities.size());

		entities = entityService.findByRelatedEntity(entity.getId(), "xx", 2, Direction.OUTGOING);
		assertEquals(0, entities.size());
	}

	@Test
	public void findByEntityIdTest() throws QuerydslException {

		Pageable pageable = PageRequest.of(0, 10);
		Page<CommEntity> page = entityService.findByEntityId("001", pageable);
		assertEquals(1, page.getContent().size());

		String queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"entityId\", \"value\": \"001\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"entityId\", \"value\": \"001/001\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(1, page.getContent().size());
		CommEntity entity = entityService.findByEntityId("001");
		Assert.assertNotNull(entity);

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.vendor\", \"value\": \"oracle\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.vv\", \"value\": 10}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(1, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"props.vendor\", \"value\": \"ora\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"conn.ip\", \"value\": \"127.0.0.1\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(1, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"conn.ip\", \"value\": \"127.0\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(1, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"name\", \"value\": \"Entity 001\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		Assert.assertNotNull(page);

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"name\", \"value\": \"Res 001\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		Assert.assertNotNull(page);

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"name\", \"value\": \"Entity\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		Assert.assertNotNull(page);

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"type\", \"value\": \"/os\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"type\", \"value\": \"/os/windows\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(2, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"token\", \"value\": \"1234567890\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(2, page.getTotalElements());
		assertEquals(1, page.getTotalPages());

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.vendor\", \"value\": \"ora\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(0, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"props.vendor\", \"value\": \"ora\"}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getTotalElements());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"props.vendor\", \"value\": \"ora\"},\"pagination\": { \"size\": 2, \"page\": 1}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getTotalElements());
		assertEquals(2, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"props.vendor\", \"value\": \"ora\"},\"pagination\": { \"size\": 2, \"page\": 2}}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(3, page.getTotalElements());
		assertEquals(1, page.getContent().size());

		queryStr = "{\n" + "	\n" + "	\"filter\":{\n"
				+ "	\"and\": [{\"ops\":\"like\", \"field\": \"name\", \"value\": \"Entity 002\"},{\"ops\":\"like\", \"field\": \"conn.ip\", \"value\": \"127.0.0.1\"},{\"ops\":\"like\", \"field\": \"props.val\", \"value\": 10}]\n"
				+ "	},\n" + "	\"pagination\": { \"size\": 2, \"page\": 1} \n" + "}";
		page = entityService.findEntitiesByCondition(queryStr);
		assertEquals(1, page.getContent().size());

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"conn.ip\", \"value\": \"127.0.0.1\"}}";
		Map<Object, Long> counter = entityService.countEntityByPropAndCondition("status", queryStr);
		assertTrue(counter.get("green") == 1);

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.vendor\", \"value\": \"oracle\"}}";
		counter = entityService.countEntityByPropAndCondition("status", queryStr);
		assertTrue(counter.get("green") == 1);

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"props.vendor\", \"value\": \"ora\"}}";
		counter = entityService.countEntityByPropAndCondition("status", queryStr);
		assertNull(counter.get("green"));

		queryStr = "{\"filter\":{\"ops\":\"like\", \"field\": \"props.vendor\", \"value\": \"ora\"}}";
		counter = entityService.countEntityByPropAndCondition("status", queryStr);
		assertTrue(counter.get("green") == 1);

		queryStr = "{\"filter\":{\"ops\":\"eq\", \"field\": \"token\", \"value\": \"1234567890\"}}";
		counter = entityService.countEntityByPropAndCondition("type", queryStr);
		assertTrue(counter.get("/os/windows/nic") == 1);
		assertTrue(counter.get("/os/windows") == 1);

	}

}
