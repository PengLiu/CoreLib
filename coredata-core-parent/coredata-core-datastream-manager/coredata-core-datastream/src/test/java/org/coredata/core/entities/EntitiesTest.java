package org.coredata.core.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.coredata.core.TestApp;
import org.coredata.core.util.redis.service.RedisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class EntitiesTest {

	@Autowired
	private RedisService redisService;

	@Before
	public void init() {
		ResEntity res = new ResEntity();
		res.setEntityId("aabbccddeeff");
		res.setName("TestEntity 001");
		res.setToken("1234567890");
		res.setType("/os/windows");
		res.addProp("vendor", "oracle");
		res.addProp("version", "1.0.x");
		res.addProp("transformId", "windows");
		Map<String, String> conn = new HashMap<>();
		conn.put("protocol", "snmp");
		conn.put("snmp_ip", "172.16.2.22");
		conn.put("snmp_readonlycommunity", "public");
		conn.put("snmp_udpport", "161");
		conn.put("snmp_version", "1");
		Map<String, String> conn2 = new HashMap<>();
		conn2.put("protocol", "ssh");
		conn2.put("ssh_ip", "172.16.2.22");
		conn2.put("ssh_password", "qazwsx");
		res.addConn("snmp", conn);
		res.addConn("ssh", conn2);
		redisService.saveData(RedisService.INSTANCE, res.getEntityId(), res);
	}

	@Test
	public void entityTest() {
		ResEntity res = (ResEntity) redisService.loadDataByTableAndKey(RedisService.INSTANCE, "aabbccddeeff");
		assertNotNull(res);
		assertEquals("aabbccddeeff", res.getEntityId());
	}

}
