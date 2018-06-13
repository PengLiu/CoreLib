package org.coredata.core.repositories;

import org.coredata.core.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleResp extends JpaRepository<Role, Long> {

	Role findByName(String name);
}
