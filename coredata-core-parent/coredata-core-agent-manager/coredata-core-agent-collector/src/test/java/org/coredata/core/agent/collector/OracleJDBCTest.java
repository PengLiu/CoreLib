package org.coredata.core.agent.collector;

import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.coredata.core.TestApp;
import org.coredata.core.agent.collector.exception.ProtocolException;
import org.coredata.core.agent.collector.protocol.Protocol;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
public class OracleJDBCTest {

	private Map<String, String> connection = new HashMap<>();

	@Before
	public void init() {
		Protocol.Init();
		connection.put("oracle_jdbc_ip", "172.16.3.160");
		connection.put("oracle_jdbc_port", "1521");
		connection.put("oracle_jdbc_servnamemonitor", "0");
		connection.put("oracle_jdbc_sid", "orcl");
		connection.put("oracle_jdbc_dbusername", "c##deta");
		connection.put("oracle_jdbc_dbuserpwd", "qazWSX");
	}

	@Test
	public void oracleTest() throws ProtocolException {
		Protocol protocol = Protocol.getProtocolRunner(ProtocolConstants.ORACLE_JDBC);
		assertNotEquals(null, protocol);
		Cmd cmd = new Cmd();
		cmd.setConnection(connection);
		cmd.setCmd("select instance_name,version from v$instance");
		String result = protocol.run(cmd);
		System.err.println(result);
	}

}
