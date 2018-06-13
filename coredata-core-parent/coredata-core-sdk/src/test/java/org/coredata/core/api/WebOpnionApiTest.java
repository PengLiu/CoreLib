package org.coredata.core.api;

import org.coredata.core.SDKServer;
import org.coredata.core.metric.repositories.WebOpinionResp;
import org.coredata.core.metric.services.WebOpinionService;
import org.coredata.core.sdk.api.WebOpinionApi;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@EnableNeo4jRepositories(basePackages = { "org.coredata.core.entities.repositories", "org.coredata.core.model.repositories" })
@EntityScan(basePackages = { "org.coredata.core.entities", "org.coredata.core.model.entities" })
@SpringBootTest(classes = SDKServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebOpnionApiTest {

	@Autowired
	private WebOpinionService webOpinionService;

	@Autowired
	private WebOpinionApi webOpnionApi;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private WebOpinionResp webOpinionResp;

	@After
	public void cleanup() {
		webOpinionResp.deleteAll();
	}

	@Test
	public void saveOpinionTet() throws Exception {
		Assert.assertNotNull(webOpnionApi);
		ClassPathResource resource = new ClassPathResource("/opnion_data.log", getClass());
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		map.add("file", resource);
		ResponseEntity<String> response = this.restTemplate.postForEntity("/api/v1/opinion/", map, String.class);
		Assert.assertTrue(response.getStatusCode().is2xxSuccessful());

		Assert.assertEquals(65L, webOpinionService.count());

	}

}