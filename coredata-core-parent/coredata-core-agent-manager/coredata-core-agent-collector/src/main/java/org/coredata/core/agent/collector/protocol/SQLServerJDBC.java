package org.coredata.core.agent.collector.protocol;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.coredata.core.agent.collector.Cmd;
import org.coredata.core.agent.collector.ProtocolConstants;
import org.coredata.core.agent.collector.exception.ProtocolException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SQLServerJDBC extends JDBC {

	private String testSql = "select getdate() as today";

	@Override
	public String run(Cmd cmd) throws ProtocolException {
		ProtocolException tmp = null;
		try (Connection conn = getConnection(cmd)) {
			for (int i = 0; i <= cmd.getRetry(); i++) {
				try {
					Statement statement = conn.createStatement();
					statement.setQueryTimeout(cmd.getTimeInSecond());
					Map<String, ObjectNode> resultsSetMap = new HashMap<String, ObjectNode>();
					boolean hasResult = statement.execute(cmd.getCmd());
					int index = 1;
					while (hasResult) {
						ObjectNode obj = mapper.createObjectNode();
						ResultSet rs = statement.getResultSet();
						obj = toJson(rs);
						resultsSetMap.put("result_" + index, obj);
						index++;
						hasResult = statement.getMoreResults();
					}
					if (resultsSetMap.size() == 1) {
						ObjectNode obj = resultsSetMap.get("result_1");
						return mapper.writeValueAsString(obj);
					} else {
						ObjectNode multiObj = mapper.createObjectNode();
						for (String key : resultsSetMap.keySet()) {
							multiObj.set(key, resultsSetMap.get(key));
						}
						return mapper.writeValueAsString(multiObj);
					}

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
		props.setProperty("dataSourceClassName", "com.microsoft.sqlserver.jdbc.SQLServerDataSource");
		props.setProperty("dataSource.user", prop.get("sqlserver_jdbc_user"));
		props.setProperty("dataSource.password", prop.get("sqlserver_jdbc_password"));
		props.setProperty("dataSource.databaseName", prop.get("sqlserver_jdbc_dbname"));
		props.setProperty("dataSource.portNumber", prop.get("sqlserver_jdbc_port"));
		props.setProperty("dataSource.serverName", prop.get("sqlserver_jdbc_ip"));
		return super.getConnection(prop, props, cmd.getRetry(), cmd.getTimeout(), testSql);
	}

	@Override
	String buildJDBCId(Map<String, String> prop) {
		return buildJDBCUrl(prop) + prop.get("sqlserver_jdbc_user") + prop.get("sqlserver_jdbc_password");
	}

	@Override
	String buildJDBCUrl(Map<String, String> prop) {
		StringBuilder builder = new StringBuilder("jdbc:sqlserver://");
		builder.append(prop.get("sqlserver_jdbc_ip")).append(":").append(prop.get("sqlserver_jdbc_port")).append("/").append(prop.get("sqlserver_jdbc_dbname"));
		return builder.toString();
	}
}
