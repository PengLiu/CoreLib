package org.coredata.core.util;

import org.coredata.core.TestApp;
import org.coredata.core.util.redis.service.RedisService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@Ignore
public class RedisTest {

	@Autowired
	private RedisService redisService;

	@Test
	public void connTest() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			redisService.saveData("test", "key" + i, "metric" + i);
			Thread.sleep(1000);
		}
	}
}
