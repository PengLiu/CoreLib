package org.coredata.core.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.coredata.core.entities.Privilege;
import org.coredata.core.entities.Role;
import org.coredata.core.entities.User;
import org.coredata.core.repositories.PrivilegeResp;
import org.coredata.core.repositories.RoleResp;
import org.coredata.core.repositories.UserResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class InitDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	boolean alreadySetup = false;

	@Autowired
	private UserResp userResp;

	@Autowired
	private RoleResp roleResp;

	@Autowired
	private PrivilegeResp privilegeResp;

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent arg0) {

		if (alreadySetup) {
			return;
		}

		Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
		Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");

		List<Privilege> adminPrivileges = Arrays.asList(readPrivilege, writePrivilege);
		createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
		createRoleIfNotFound("ROLE_USER", Arrays.asList(readPrivilege));
		createAdminUserIfNotFound("admin");

		alreadySetup = true;

	}

	@Transactional
	private User createAdminUserIfNotFound(String name) {
		User user = userResp.findByUserName("admin");
		if (user != null) {
			return user;
		}
		Role adminRole = roleResp.findByName("ROLE_ADMIN");
		user = new User();
		user.setUserName("admin");
		user.setPassword(DigestUtils.sha1Hex("123456"));
		user.setRoles(Arrays.asList(adminRole));
		user.setEnabled(true);
		userResp.save(user);
		return user;
	}

	@Transactional
	private Privilege createPrivilegeIfNotFound(String name) {

		Privilege privilege = privilegeResp.findByName(name);
		if (privilege == null) {
			privilege = new Privilege(name);
			privilegeResp.save(privilege);
		}
		return privilege;
	}

	@Transactional
	private Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {

		Role role = roleResp.findByName(name);
		if (role == null) {
			role = new Role(name);
			role.setPrivileges(privileges);
			roleResp.save(role);
		}
		return role;
	}

}
