package org.coredata.core.api;

import org.coredata.core.SDKServer;
import org.coredata.core.alarm.documents.Alarm;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SDKServer.class)
public class AlarmApiTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Ignore
	public void saveAlarmTest() throws Exception {

		Alarm alarm = new Alarm();
		alarm.setAlarmRuleId("rule001");
		alarm.setContent("Test alarm content.");

		RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/v1/alarms/").accept(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsBytes(alarm)).contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		Assert.assertEquals(HttpStatus.OK.value(), response.getStatus());

		alarm = mapper.readValue(result.getResponse().getContentAsString(), Alarm.class);
		Assert.assertNotNull(alarm.getId());

	}

}
