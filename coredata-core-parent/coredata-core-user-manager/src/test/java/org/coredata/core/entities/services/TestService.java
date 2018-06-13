package org.coredata.core.entities.services;

import javax.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TestService {

	@PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
	public String writeMethod() {
		return "OK";
	}

	@PreAuthorize("hasAuthority('READ_PRIVILEGE')")
	public String readMethod() {
		return "OK";
	}

}
