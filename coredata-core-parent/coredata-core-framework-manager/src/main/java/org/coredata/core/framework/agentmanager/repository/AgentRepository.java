package org.coredata.core.framework.agentmanager.repository;

import org.coredata.core.framework.agentmanager.dto.AgentDto;
import org.coredata.core.framework.agentmanager.entity.Agent;
import org.coredata.core.framework.agentmanager.page.PageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent相关repository接口实现类
 * @author sushi
 *
 */
@Repository
public class AgentRepository  {

	@Autowired
	private BaseRepository baseRepository;

	
	public List<Agent> findAllAgents() {
		String sql = "SELECT * FROM t_agent ";
		List<Agent> query = baseRepository.find(sql, null, Agent.class);
		return query;
	}

	/**
	 *
	 */
	
	public Map<String, Object> findAgentSettingByAgentId(Long agentId) {
		String sql = "SELECT s.agent_id AS agent_id,s.setting_key AS setting_key,s.setting_value AS setting_value,a.remarks AS remarks FROM t_agent a LEFT JOIN t_agent_setting s ON a.id = s.agent_id WHERE s.agent_id = ? ";
		Map<String, Object> query = baseRepository.queryForCustomMap(sql, new Object[] { agentId }, new ResultSetExtractor<Map<String, Object>>() {
			
			public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
				HashMap<String, Object> mapRet = new HashMap<String, Object>();
				while (rs.next()) {
					mapRet.put("agent_id", rs.getString("agent_id"));
					mapRet.put(rs.getString("setting_key"), rs.getString("setting_value"));
					mapRet.put("remarks", rs.getString("remarks"));
				}
				return mapRet;
			}
		});
		return query;
	}

	
	public PageParam<Agent> findPagingAgent(AgentDto dto) {
		String sql = "SELECT * FROM t_agent ORDER BY ID ";
		PageParam<Agent> queryPagination = baseRepository.queryPagination(sql, null, dto.getPageNum(), dto.getPageSize(), Agent.class);
		return queryPagination;
	}

	
	public void addAgentInfo(Object[] params) {
		String sql = "INSERT INTO t_agent (type,ip_address,status,info,create_time,last_online_time,features) VALUES (?,?,?,?,?,?,?) ";
		baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public Agent findAgentByIpCredential(String ip, String credential) {
		String sql = "SELECT * FROM t_agent a WHERE a.ip_address = ? and a.info = ?";
		List<Agent> agents = baseRepository.find(sql, new Object[] { ip, credential }, Agent.class);
		if (CollectionUtils.isEmpty(agents))
			return null;
		return agents.get(0);
	}

	
	public List<Long> findAgentIdByIp(String ip) {
		String sql = "SELECT a.id FROM t_agent a WHERE a.ip_address in (" + ip + ")";
		List<Long> agentIds = baseRepository.queryForList(sql, null, Long.class);
		return CollectionUtils.isEmpty(agentIds) ? null : agentIds;
	}

	
	public int updateAgentLastOnlineTime(Object[] params) {
		String sql = "UPDATE t_agent SET last_online_time = ? WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public int updateAgentLastOfflineTime(Object[] params) {
		String sql = "UPDATE t_agent SET last_offline_time = ? WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public Agent findAgentById(Long agentId) {
		String sql = "SELECT * FROM t_agent a WHERE a.id = ? ";
		List<Agent> agents = baseRepository.find(sql, new Object[] { agentId }, Agent.class);
		return CollectionUtils.isEmpty(agents) ? null : agents.get(0);
	}

	
	public void deleteAgentSettingByAgentId(Long agentId) {
		String sql = "DELETE FROM t_agent_setting WHERE agent_id = ? ";
		baseRepository.addUpdateOrDelete(sql, new Object[] { agentId });
	}

	
	public int saveAgentSetting(List<Object[]> params) {
		String sql = "INSERT INTO t_agent_setting (agent_id,setting_key,setting_value) VALUES (?,?,?)";
		return baseRepository.batchAddUpdateOrDelete(sql, params);
	}

	
	public int updateAgentStatus(Object[] params) {
		String sql = "UPDATE t_agent SET status = ? WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public int updateAgentRemarks(Object[] params) {
		String sql = "UPDATE t_agent SET remarks = ? WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public int findAgentsCount() {
		String sql = "SELECT COUNT(*) FROM t_agent ";
		Integer count = baseRepository.queryForCount(sql, null);
		return count.intValue();
	}

	
	public int findAgentCountByIp(String ip) {
		String sql = "SELECT COUNT(*) FROM t_agent WHERE ip_address = ? ";
		Integer count = baseRepository.queryForCount(sql, new Object[] { ip });
		return count.intValue();
	}

	
	public int saveAgentHostInfo(Object[] params) {
		String sql = "INSERT INTO t_agent_hostinfo (agent_id,hostinfo,ping_time) VALUES (?,?,?) ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public List<Agent> findAgentByFeatures(String features, Object[] params) {
		String sql = "SELECT * FROM t_agent a WHERE a.features LIKE '%" + features + "%' AND a.status = ? ORDER BY a.current_task ";
		List<Agent> agents = baseRepository.find(sql, params, Agent.class);
		return CollectionUtils.isEmpty(agents) ? null : agents;
	}

	
	public int updateAgentCurrentTask(Object[] params) {
		String sql = "UPDATE t_agent SET current_task = (IFNULL(current_task,0)+1) WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public int updateAgentCurrentTaskByIp(List<Object[]> params) {
		String sql = "UPDATE t_agent SET current_task = ? WHERE ip_address = ? ";
		return baseRepository.batchAddUpdateOrDelete(sql, params);
	}

	
	public int updateAgentFeatures(Object[] params) {
		String sql = "UPDATE t_agent SET features = ? WHERE id = ? ";
		return baseRepository.addUpdateOrDelete(sql, params);
	}

	
	public int removeByIp(String ip) {
		String sql = "DELETE FROM t_agent  where ip_address = ? ";
		return baseRepository.addUpdateOrDelete(sql, new Object[] { ip });
	}

	
	public List<Agent> findAgentsByCustomerId(String customerId) {
		String sql = "SELECT * FROM t_agent a WHERE a.info = '" + customerId + "'";
		List<Agent> query = baseRepository.find(sql, null, Agent.class);
		return query;
	}

}
