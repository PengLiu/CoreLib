package org.coredata.core.framework.agentmanager.repository;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.framework.agentmanager.entity.AgentTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Repository
public class AgentTaskRepository  {

	@Autowired
	private BaseRepository baseRepository;

	
	public String getAllNoAgentTaskIds(boolean needRetry, int retryTime) {
		StringBuilder sql = new StringBuilder("SELECT id FROM t_agent_task WHERE status = 0 AND signature IS NULL ORDER BY id ");
		if (needRetry)
			sql.append("AND retry < ").append(retryTime);
		List<Long> ids = baseRepository.queryForList(sql.toString(), null, Long.class);
		if (CollectionUtils.isEmpty(ids))
			return null;
		return StringUtils.join(ids, ",");
	}

	
	public int updateAgentSignature(String ids) {
		String sql = "UPDATE t_agent_task SET signature = " + System.currentTimeMillis() + " WHERE signature IS NULL AND id in (" + ids + ")";
		return baseRepository.addUpdateOrDelete(sql, null);
	}

	
	public AgentTask findAgentTaskById(Long taskId) {
		String sql = "SELECT * FROM t_agent_task WHERE id = ? ";
		List<AgentTask> agentTasks = baseRepository.find(sql, new Object[] { taskId }, AgentTask.class);
		return CollectionUtils.isEmpty(agentTasks) ? null : agentTasks.get(0);
	}

	
	public int updateAgentTaskAgentId(Object[] params) {
		String sql = "UPDATE t_agent_task SET agentId = ?,status = 1 WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public int batchUpdateAgentTaskAbandon(List<Object[]> params, boolean needRetry) {
		StringBuilder sql = new StringBuilder("UPDATE t_agent_task SET status = 0,agentId = NULL,signature = NULL ");
		if (needRetry)
			sql.append(",retry = (retry + 1) ");
		sql.append(" WHERE id = ? ");
		return baseRepository.batchAddUpdateOrDelete(sql.toString(), params);
	}

	
	public int insertAgentTask(List<Object[]> params) {
		String sql = "INSERT INTO t_agent_task (instanceId,status,protocol,createTime) VALUES (?,?,?,?) ";
		return baseRepository.batchAddUpdateOrDelete(sql, params);
	}

	
	public int batchUpdateAgentTaskPending(List<Object[]> params) {
		String sql = "UPDATE t_agent_task SET status = 0,agentId = NULL,signature = NULL,retry = 0 WHERE agentId = ? ";
		return baseRepository.batchAddUpdateOrDelete(sql, params);
	}

	
	public AgentTask getAgentTaskByProtocolAndInstId(Object[] params) {
		String sql = "SELECT * FROM t_agent_task WHERE instanceId = ? AND protocol = ? ";
		List<AgentTask> agentTasks = baseRepository.find(sql, params, AgentTask.class);
		return CollectionUtils.isEmpty(agentTasks) ? null : agentTasks.get(0);
	}

	
	public int updateAgentTaskPending(Object[] param) {
		String sql = "UPDATE t_agent_task SET status = 0,agentId = NULL,signature = NULL,retry = 0 WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, param);
	}

	
	public int cancelTask(String instId) {
		String sql = "UPDATE t_agent_task SET status = 2,agentId = NULL,signature = NULL,retry = 0 WHERE instanceId = ? ";
		return baseRepository.addUpdateOrDelete(sql, new Object[] { instId });
	}

	
	public List<AgentTask> findInstTasks(String instId) {
		String sql = "SELECT * FROM t_agent_task where t_agent_task.instanceId = ?";
		return baseRepository.find(sql, new Object[] { instId }, AgentTask.class);
	}

	
	public int addMonitor(String instId) {
		String sql = "UPDATE t_agent_task SET status = 0, agentId = NULL,signature = NULL,retry = 0 WHERE instanceId = ? AND status = 2 ";
		return baseRepository.addUpdateOrDelete(sql, new Object[] { instId });
	}

	
	public int removeInstanceTasks(String inst) {
		String sql = "DELETE FROM t_agent_task WHERE instanceId = ?";
		return baseRepository.addUpdateOrDelete(sql, new Object[] { inst });
	}

	
	public void deleteTaskById(Long id) {
		String sql = "DELETE FROM t_agent_task WHERE id = ?";
		baseRepository.addUpdateOrDelete(sql, new Object[] { id });
	}

	
	public void updateCollectCmd() {
		String sql = "UPDATE t_agent_task SET status = 0, agentId = NULL,signature = NULL,retry = 0 WHERE (status = 1 OR status = 0) ";
		baseRepository.addUpdateOrDelete(sql, null);
	}

}
