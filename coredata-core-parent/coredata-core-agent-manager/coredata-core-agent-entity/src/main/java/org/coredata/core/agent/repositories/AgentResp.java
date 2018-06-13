package org.coredata.core.agent.repositories;

import org.coredata.core.agent.entities.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentResp extends JpaRepository<Agent, Long> {

	Page<Agent> findByToken(String token, Pageable pageable);

	void removeByIpAddress(String ipAddr);

	@Query(value = "FROM Agent WHERE features LIKE %:feature% AND status = :status", countQuery = "SELECT count(Agent) FROM Agent WHERE features LIKE %:feature% AND status = :status ")
	Page<Agent> findByFeatures(@Param(value = "feature") String feature, @Param(value = "status") int status, Pageable pageable);

}