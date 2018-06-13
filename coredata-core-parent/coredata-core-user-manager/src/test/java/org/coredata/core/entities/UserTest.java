package org.coredata.core.entities;

import javax.transaction.Transactional;

import org.coredata.core.TestApp;
import org.coredata.core.entities.services.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class UserTest {

	@Autowired
	private TestService testService;

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void accessDeniedTest() {
		testService.readMethod();
	}

	@Test
	@WithMockUser(username = "admin", password = "admin", authorities = { "READ_PRIVILEGE", "WRITE_PRIVILEGE" })
	public void accessSuccessTest() {
		testService.writeMethod();
	}

	@Test
	@WithUserDetails(value = "admin", userDetailsServiceBeanName = "userService")
	public void newUserTest() {
		testService.writeMethod();
	}

}
