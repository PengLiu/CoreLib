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
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;

public class MySqlJDBC extends JDBC {

	private String testSql = "select 1 from dual";

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
		props.setProperty("dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		props.setProperty("dataSource.user", prop.get("mysql_jdbc_user"));
		props.setProperty("dataSource.password", prop.get("mysql_jdbc_password"));
		props.setProperty("dataSource.databaseName", prop.get("mysql_jdbc_dbname"));
		props.setProperty("dataSource.portNumber", prop.get("mysql_jdbc_port"));
		props.setProperty("dataSource.serverName", prop.get("mysql_jdbc_ip"));

		return super.getConnection(prop, props, cmd.getRetry(), cmd.getTimeout(), testSql);

	}

	@Override
	String buildJDBCId(Map<String, String> prop) {
		return buildJDBCUrl(prop) + prop.get("mysql_jdbc_user") + prop.get("mysql_jdbc_password");
	}

	@Override
	String buildJDBCUrl(Map<String, String> prop) {
		StringBuilder builder = new StringBuilder("jdbc:mysql://");
		builder.append(prop.get("mysql_jdbc_ip")).append(":").append(prop.get("mysql_jdbc_port")).append("/").append(prop.get("mysql_jdbc_dbname"));
		return builder.toString();
	}

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
	ProtocolException buildException(Exception e, String cmd) {
		if (e instanceof PoolInitializationException) {
			return new ProtocolException(ProtocolException.Type.ConnErr, ProtocolException.ErrNum.Conn_Timeout, i18n.getMsg("jdbc.err.conntimeout"), cmd, e);
		}
		return super.buildException(e, cmd);
	}

}
