package org.coredata.core.repositories;

import org.coredata.core.entities.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeResp extends JpaRepository<Privilege, Long> {
	
	Privilege findByName(String name);

}
