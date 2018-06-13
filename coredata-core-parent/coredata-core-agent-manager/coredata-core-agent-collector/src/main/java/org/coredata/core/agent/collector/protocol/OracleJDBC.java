package org.coredata.core.agent.collector.protocol;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.coredata.core.agent.collector.Cmd;
import org.coredata.core.agent.collector.ProtocolConstants;
import org.coredata.core.agent.collector.exception.ProtocolException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OracleJDBC extends JDBC {

	private String testSql = "select sysdate from dual;";

	@Override
	public String run(Cmd cmd) throws ProtocolException {
		ProtocolException tmp = null;

		try (Connection conn = getConnection(cmd)) {
			for (int i = 0; i <= cmd.getRetry(); i++) {
				try {
					Statement statement = conn.createStatement();
					statement.setQueryTimeout(cmd.getTimeInSecond());
					java.sql.ResultSet resultSet = statement.executeQuery(cmd.getCmd());
					ObjectNode obj = toJson(resultSet);
					return mapper.writeValueAsString(obj);
				} catch (SQLException e) {
					tmp = buildException(e, cmd.getCmd());
				} catch (JsonProcessingException e) {
					tmp = new ProtocolException(ProtocolException.Type.CollErr, ProtocolException.ErrNum.Coll_CMD_Err, i18n.getMsg("json.format.error"),
							cmd.getCmd(), e);
				}
			}
		} catch (SQLException e1) {
			tmp = buildException(e1, cmd.getCmd());
		}

		throw tmp;
	}

	@Override
	public String healthCheck(Cmd cmd) throws ProtocolException {
		if (ProtocolConstants.HEALTH_CHECK_PREFIX.equals(cmd.getCmd())) {
			cmd.setCmd(testSql);
		}
		return run(cmd);
	}

	private Connection getConnection(Cmd cmd) throws ProtocolException {
		Map<String, String> prop = cmd.getConnection();
		Properties props = new Properties();
		props.setProperty("dataSourceClassName", "oracle.jdbc.pool.OracleDataSource");
		props.setProperty("dataSource.user", prop.get("oracle_jdbc_dbusername"));
		props.setProperty("dataSource.password", prop.get("oracle_jdbc_dbuserpwd"));
		props.setProperty("dataSource.databaseName",
				prop.get("oracle_jdbc_servnamemonitor").equals("1") ? prop.get("oracle_jdbc_servicename") : prop.get("oracle_jdbc_sid"));
		props.setProperty("dataSource.portNumber", prop.get("oracle_jdbc_port"));
		props.setProperty("dataSource.serverName", prop.get("oracle_jdbc_ip"));
		props.setProperty("dataSource.driverType", "thin");
		return super.getConnection(prop, props, cmd.getRetry(), cmd.getTimeout(), testSql);
	}

	@Override
	String buildJDBCId(Map<String, String> prop) {
		return buildJDBCUrl(prop) + prop.get("oracle_jdbc_dbusername") + prop.get("oracle_jdbc_dbuserpwd");
	}

	@Override
	String buildJDBCUrl(Map<String, String> prop) {
		StringBuilder builder = new StringBuilder("jdbc:oracle:thin:@");
		builder.append(prop.get("oracle_jdbc_ip")).append(":").append(prop.get("oracle_jdbc_port")).append(":")
				.append(prop.get("oracle_jdbc_servnamemonitor").equals("1") ? prop.get("oracle_jdbc_servicename") : prop.get("oracle_jdbc_sid"));
		return builder.toString();
	}

}
