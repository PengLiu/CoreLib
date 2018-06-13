package org.coredata.core.repositories;

import org.coredata.core.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserResp extends JpaRepository<User, Long> {

	User findByUserName(String userName);
}
