package org.coredata.core.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.coredata.core.TestApp;
import org.coredata.core.agent.entities.Agent;
import org.coredata.core.agent.entities.AgentTask;
import org.coredata.core.agent.services.AgentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit4.SpringRunner;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class AgentTest {

	@Autowired
	private AgentService agentService;

	@Test
	public void agentQueryTest() throws InterruptedException {

		Agent agent = new Agent();
		agent.setCreateTime(System.currentTimeMillis());
		agent.setInfo("info001");
		agent.setIpAddress("127.0.0.1");
		agent.setStatus(1);
		agentService.saveAgent(agent);

		Thread.sleep(500);

		agent = new Agent();
		agent.setCreateTime(System.currentTimeMillis());
		agent.setInfo("info002");
		agent.setIpAddress("192.0.0.1");
		agent.setStatus(1);
		agent.setFeatures("snmp");
		agentService.saveAgent(agent);

		Pageable pageable = PageRequest.of(0, 10, Direction.ASC, "createTime");

		Map<String, Object> condition = new HashMap<>();
		condition.put("status", 1);
		Page<Agent> result = agentService.findByCondition(condition, pageable);
		assertEquals(2, result.getContent().size());
		assertEquals("info001", result.getContent().iterator().next().getInfo());

		result = agentService.findAllAgents(pageable);
		assertEquals(2, result.getContent().size());
		assertEquals(1, result.getTotalPages());

		condition = new HashMap<>();
		condition.put("info", "info001");

		result = agentService.findByCondition(condition, pageable);
		assertEquals(1, result.getContent().size());
		assertEquals(1, result.getTotalPages());

		result = agentService.findByCondition(condition, pageable);
		assertEquals(1, result.getContent().size());
		assertEquals(1, result.getTotalPages());

		agent = result.getContent().iterator().next();

		AgentTask task = new AgentTask();
		task.setAgent(agent);
		task.setEntityId("entityId001");
		task.setProtocol("snmp");
		task.setStatus(0);
		task.setCreateTime(System.currentTimeMillis());

		agent.addTask(task);
		agentService.saveAgent(agent);

		condition = new HashMap<>();
		condition.put("info", "info001");
		result = agentService.findByCondition(condition, pageable);
		assertEquals(1, result.getContent().size());
		assertEquals(1, result.getTotalPages());
		assertEquals(1, result.getContent().iterator().next().getTaskes().size());

		List<Agent> agents = new ArrayList<>();

		Agent tmp = new Agent();
		tmp.setCreateTime(System.currentTimeMillis());
		tmp.setInfo("info003");
		tmp.setIpAddress("192.168.0.1");
		tmp.setToken("1234567890");
		tmp.setFeatures("snmp jdbc");
		tmp.setStatus(0);
		agents.add(tmp);

		tmp = new Agent();
		tmp.setCreateTime(System.currentTimeMillis());
		tmp.setInfo("info004");
		tmp.setIpAddress("192.168.0.2");
		tmp.setToken("1234567890");
		tmp.setStatus(0);
		agents.add(tmp);

		agentService.saveAgent(agents);

		Page<AgentTask> taskes = agentService.findTaskByEntity("entityId001", pageable);
		assertEquals(1, taskes.getContent().size());
		assertEquals(task.getProtocol(), taskes.getContent().iterator().next().getProtocol());

		assertEquals(agent.getId(), agentService.findById(agent.getId()).getId());
		assertEquals(4, agentService.findAgentsCount());

		agentService.removeByIp("127.0.0.1");
		assertEquals(3, agentService.findAgentsCount());

		assertEquals(tmp.getId(), agentService.findById(tmp.getId()).getId());

		agentService.removeById(tmp.getId());
		assertEquals(2, agentService.findAgentsCount());
		assertNull(agentService.findById(tmp.getId()));

		assertEquals(1, agentService.findByToken("1234567890", pageable).getContent().size());

		assertEquals(1, agentService.findByFeatureAndStatus("snmp", 1, pageable).getContent().size());
		assertEquals(1, agentService.findByFeatureAndStatus("snmp", 0, pageable).getContent().size());
		assertEquals(0, agentService.findByFeatureAndStatus("jdbc", 1, pageable).getContent().size());

		condition = new HashMap<>();
		condition.put("info", "info002");
		assertEquals(1, agentService.findCountByCondition(condition));

	}

}
