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
public class SqlServerJDBCTest {

	private Map<String, String> connection = new HashMap<>();

	@Before
	public void init() {
		Protocol.Init();
		connection.put("sqlserver_jdbc_user", "sa");
		connection.put("sqlserver_jdbc_password", "1qaz@WSX");
		connection.put("sqlserver_jdbc_ip", "172.16.3.160");
		connection.put("sqlserver_jdbc_port", "1433");
		connection.put("sqlserver_jdbc_dbname", "master");
	}

	@Test
	public void sqlserverTest() throws ProtocolException {
		Protocol protocol = Protocol.getProtocolRunner(ProtocolConstants.SQLSERVER_JDBC);
		assertNotEquals(null, protocol);
		Cmd cmd = new Cmd();
		cmd.setConnection(connection);
		cmd.setCmd("select @@version as version;");
		String result = protocol.run(cmd);
		System.err.println(result);
	}

}
