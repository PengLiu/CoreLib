package org.coredata.core.agent.collector.protocol;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.coredata.core.agent.collector.exception.ProtocolException;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public abstract class JDBC extends Protocol {

	Map<String, HikariDataSource> dsCache = new ConcurrentHashMap<>();

	abstract String buildJDBCId(Map<String, String> prop);

	abstract String buildJDBCUrl(Map<String, String> prop);

	/**
	 * SQL多个结果集按列名整合到一个ObjectNode中
	 * 重名的列名后拼接结果集序号
	 * @param rs 结果集
	 * @param results ObjectNode
	 * @param index 结果集序号
	 * @throws SQLException
	 */
	public void toJson(ResultSet rs, ObjectNode results, int index) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int numColumns = rsmd.getColumnCount();
		List<String> columnNames = new ArrayList<String>();
		for (int i = 1; i < numColumns + 1; i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (results.has(columnName))
				columnName = columnName + "_" + index;
			columnNames.add(columnName);
			results.set(columnName, mapper.createArrayNode());
		}

		while (rs.next()) {
			for (int i = 1; i < numColumns + 1; i++) {
				String column_name = columnNames.get(i - 1);
				ArrayNode obj = (ArrayNode) results.get(column_name);
				if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
					obj.add(mapper.valueToTree(rs.getArray(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
					obj.add(mapper.valueToTree(rs.getLong(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
					obj.add(mapper.valueToTree(rs.getBoolean(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
					obj.add(mapper.valueToTree(rs.getBlob(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
					obj.add(mapper.valueToTree(rs.getDouble(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
					obj.add(mapper.valueToTree(rs.getFloat(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
					obj.add(mapper.valueToTree(rs.getInt(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
					obj.add(mapper.valueToTree(rs.getNString(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
					obj.add(mapper.valueToTree(rs.getString(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
					obj.add(mapper.valueToTree(rs.getInt(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
					obj.add(mapper.valueToTree(rs.getInt(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
					obj.add(mapper.valueToTree(rs.getDate(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
					obj.add(mapper.valueToTree(rs.getTimestamp(i)));
				} else {
					obj.add(mapper.valueToTree(rs.getObject(i)));
				}
			}
		}
	}

	public ObjectNode toJson(ResultSet rs) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int numColumns = rsmd.getColumnCount();

		ObjectNode results = mapper.createObjectNode();
		for (int i = 1; i < numColumns + 1; i++) {
			results.set(rsmd.getColumnLabel(i), mapper.createArrayNode());
		}

		while (rs.next()) {
			for (int i = 1; i < numColumns + 1; i++) {
				String column_name = rsmd.getColumnLabel(i);
				ArrayNode obj = (ArrayNode) results.get(column_name);
				if (rsmd.getColumnType(i) == java.sql.Types.ARRAY) {
					obj.add(mapper.valueToTree(rs.getArray(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.BIGINT) {
					obj.add(mapper.valueToTree(rs.getLong(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
					obj.add(mapper.valueToTree(rs.getBoolean(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.BLOB) {
					obj.add(mapper.valueToTree(rs.getBlob(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
					obj.add(mapper.valueToTree(rs.getDouble(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
					obj.add(mapper.valueToTree(rs.getFloat(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
					obj.add(mapper.valueToTree(rs.getInt(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
					obj.add(mapper.valueToTree(rs.getNString(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
					obj.add(mapper.valueToTree(rs.getString(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
					obj.add(mapper.valueToTree(rs.getInt(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
					obj.add(mapper.valueToTree(rs.getInt(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
					obj.add(mapper.valueToTree(rs.getDate(i)));
				} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
					obj.add(mapper.valueToTree(rs.getTimestamp(i)));
				} else {
					obj.add(mapper.valueToTree(rs.getObject(i)));
				}
			}
		}

		return results;
	}

	@Override
	ProtocolException buildException(Exception e, String cmd) {
		ProtocolException.Type type = ProtocolException.Type.UnKnowErr;
		ProtocolException.ErrNum num = null;
		String msg = "";
		if (e instanceof SQLException) {
			SQLException sqlException = (SQLException) e;
			if (sqlException.getSQLState() != null) {
				switch (sqlException.getSQLState()) {
				case "28000":
					type = ProtocolException.Type.PermissionErr;
					num = ProtocolException.ErrNum.Auth_Err;
					msg = i18n.getMsg("jdbc.err.28000");
					break;
				case "42000":
					type = ProtocolException.Type.CollErr;
					num = ProtocolException.ErrNum.Coll_Syntax_Err;
					msg = i18n.getMsg("jdbc.err.42000");
					break;
				}
			}
			msg = e.getMessage();
		}
		return new ProtocolException(type, num, msg, cmd, e);
	}

	Connection getConnection(Map<String, String> prop, Properties props, int retry, long timeOutMs, String testSql) throws ProtocolException {

		ProtocolException tmp = null;

		String url = buildJDBCUrl(prop);
		String urlId = buildJDBCId(prop);

		HikariDataSource ds = dsCache.get(urlId);
		if (ds == null) {
			HikariConfig config = new HikariConfig(props);
			try {
				ds = new HikariDataSource(config);
				dsCache.put(urlId, ds);
			} catch (Exception e) {
				tmp = new ProtocolException(ProtocolException.Type.ConnErr, "Create connection error.");
				ds = null;
				throw tmp;
			}
		}

		try {
			return ds.getConnection();
		} catch (Exception e) {
			tmp = buildException(e, url);
		}

		for (int i = 0; i < retry; i++) {
			try {
				return ds.getConnection();
			} catch (Exception e) {
				tmp = buildException(e, url);
			}
		}

		HikariDataSource tmpDs = dsCache.remove(urlId);
		if (tmpDs != null && !tmpDs.isClosed()) {
			tmpDs.close();
			tmpDs = null;
		}

		throw tmp;
	}
}
