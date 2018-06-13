package org.coredata.core.util.nlp.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.coredata.core.TestApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class NLPServiceTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private NLPService nlpService;

	@Test
	public void extractSummaryTest() throws Exception {

		try (InputStream is = NLPServiceTest.class.getResourceAsStream("/opnion_data.log");
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			String content = null;
			while ((content = reader.readLine()) != null) {
				JsonNode doc = mapper.readTree(content);
				String tmp = doc.get("content").asText();
				System.err.println("source:  " + tmp);
				System.err.println("summary: " + nlpService.extractSummary(tmp, 5));
				System.err.println("keyword: " + nlpService.extractKeyword(tmp, 5));
			}
		}
	}

}
