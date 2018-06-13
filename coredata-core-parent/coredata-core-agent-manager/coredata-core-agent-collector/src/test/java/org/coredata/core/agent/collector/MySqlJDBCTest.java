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
public class MySqlJDBCTest {

	private Map<String, String> connection = new HashMap<>();

	@Before
	public void init() {
		Protocol.Init();
		connection.put("mysql_jdbc_user", "root");
		connection.put("mysql_jdbc_password", "passed");
		connection.put("mysql_jdbc_ip", "172.16.3.113");
		connection.put("mysql_jdbc_port", "3306");
		connection.put("mysql_jdbc_dbname", "mysql");
	}

	@Test
	public void mysqlTest() throws ProtocolException {
		Protocol protocol = Protocol.getProtocolRunner(ProtocolConstants.MYSQL_JDBC);
		assertNotEquals(null, protocol);
		Cmd cmd = new Cmd();
		cmd.setConnection(connection);
		cmd.setCmd("select 1 from dual;");
		String result = protocol.run(cmd);
		System.err.println(result);
		cmd.setCmd("show variables like 'version' ");
		result = protocol.run(cmd);
		System.err.println(result);
	}

}
