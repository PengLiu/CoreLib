package org.coredata.core.agent.repositories;

import org.coredata.core.agent.entities.AgentTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentTaskResp extends JpaRepository<AgentTask, Long> {

	Page<AgentTask> findByEntityId(String entityId, Pageable pageable);

}