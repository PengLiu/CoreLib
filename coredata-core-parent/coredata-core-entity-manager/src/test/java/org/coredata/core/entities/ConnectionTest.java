package org.coredata.core.entities;

import org.coredata.core.TestApp;
import org.coredata.core.entities.services.EntityService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("localhost")
@Ignore
public class ConnectionTest {

	@Autowired
	private EntityService entityService;

	@Test
	@Rollback(false)
	public void reconnectTest() throws InterruptedException {

		for (int i = 0; i < 100; i++) {

			try {
				CommEntity entity = new CommEntity();
				entity.setEntityId("001" + i);
				entity.setName("Entity 001");
				entity.setToken("1234567890");
				entity.setType("/os/windows/nic");
				entity.addProp("vendor", "oracle");
				entity.addProp("version", "1.0.x");
				entity.addProp("isAsset", "true");
				entity.addProp("vv", 10L);
				entityService.save(entity);
			} catch (Exception e) {
				System.err.println("-------");
			} finally {
				Thread.sleep(1000);
			}
		}
	}

}
