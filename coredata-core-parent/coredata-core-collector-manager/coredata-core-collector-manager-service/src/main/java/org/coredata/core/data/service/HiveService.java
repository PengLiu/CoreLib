package org.coredata.core.data.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.coredata.core.data.vo.ColumnMeta;
import org.coredata.core.data.vo.HDFSMeta;
import org.coredata.core.data.vo.TableMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class HiveService {

	// private Logger logger = LoggerFactory.getLogger(HiveService.class);

	private ObjectMapper mapper = new ObjectMapper();

	@Value(value = "${hive.driver-class-name}")
	private String driver;

	@Value(value = "${hive.url}")
	private String url;

	@Value(value = "${hive.username}")
	private String username;

	@Value(value = "${hive.password}")
	private String password;

	private DataSource dataSource;

	@PostConstruct
	public void init() {
		dataSource = new DataSource();
		dataSource.setUrl(url);
		dataSource.setDriverClassName(driver);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
	}

	public JsonNode runSql(String sql, String db) throws SQLException {
		ArrayNode array = mapper.createArrayNode();
		Connection conn = null;
		Statement stat = null;
		ResultSet result = null;
		try {
			conn = dataSource.getConnection();
			stat = conn.createStatement();
			if (!StringUtils.isEmpty(db)) {
				stat.execute("USE " + db);
			}
			result = stat.executeQuery(sql);
			ResultSetMetaData metaData = result.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (result.next()) {
				ObjectNode rsJson = mapper.createObjectNode();
				for (int i = 1; i <= columnCount; i++) {
					String columnName = metaData.getColumnLabel(i);
					String value = result.getString(columnName);
					rsJson.put(columnName, value);
				}
				array.add(rsJson);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					;
				}
				result = null;
			}
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
		return array;
	}

	public JsonNode runSql(String sql) throws SQLException {
		return runSql(sql, null);
	}

	public void createTable(TableMeta tableMeta, HDFSMeta hdfsMeta, boolean createIfExist) throws Exception {
		Connection conn = null;
		Statement stat = null;
		try {
			conn = dataSource.getConnection();
			stat = conn.createStatement();

			StringBuilder builder = new StringBuilder();
			String db = tableMeta.getDbName();
			// 检查数据库是否存在
			if (StringUtils.isNotEmpty(db)) {
				builder.append("create database if not exists ").append(db);
				stat.execute(builder.toString());
				builder = new StringBuilder();
			} else {
				throw new Exception("db name is empty");
			}
			if (createIfExist) {
				// drop table first
				builder.append("DROP TABLE IF EXISTS ").append(tableMeta.getName());
				stat.execute(builder.toString());
				builder = new StringBuilder();
			}

			builder.append("CREATE EXTERNAL TABLE IF NOT EXISTS ");
			builder.append(tableMeta.getDbName()).append(".").append(tableMeta.getName()).append("(");
			List<ColumnMeta> columns = tableMeta.getColumns();
			List<ColumnMeta> temp = new ArrayList<>();
			for (int i = 0; i < columns.size(); i++) {
				ColumnMeta cm = columns.get(i);
				if (cm.isUse()) {
					temp.add(columns.get(i));
				}
			}
			for (int i = 0; i < temp.size(); i++) {
				ColumnMeta cm = temp.get(i);
				builder.append(cm.getName()).append(" ").append(cm.getType());
				int columnSize = cm.getSize();
				if(columnSize > 0 ) {
					builder.append("(").append(columnSize).append(")");
				}
				if (!StringUtils.isEmpty(cm.getComment())) {
					builder.append(" COMMENT '").append(cm.getComment()).append("'");
				}
				if (i != temp.size() - 1) {
					builder.append(",");
				}
			}
			builder.append(") ROW FORMAT DELIMITED FIELDS TERMINATED BY '" + hdfsMeta.getFieldSplitter()
					+ "' LINES TERMINATED BY '" + hdfsMeta.getLineSplitter() + "' STORED AS TEXTFILE LOCATION '")
					.append(hdfsMeta.getPath()).append("'");

			stat.execute(builder.toString());

		} catch (SQLException e) {
			throw e;
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
	}

	public void createTable(TableMeta tableMeta, HDFSMeta hdfsMeta) throws Exception {
		createTable(tableMeta, hdfsMeta, false);
	}

	public TableMeta showColumns(String table, String database) throws SQLException {
		TableMeta tableMeta = new TableMeta();
		tableMeta.setName(table);
		tableMeta.setDbName(database);
		tableMeta.setName(table);

		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			conn = dataSource.getConnection();
			String sql = "DESCRIBE " + database + "." + table;
			stat = conn.prepareStatement(sql);
			result = stat.executeQuery();
			while (result.next()) {
				ColumnMeta cm = new ColumnMeta(result.getString("col_name"), result.getString("data_type"),
						result.getString("comment"));
				tableMeta.addColumn(cm);
			}
			return tableMeta;
		} catch (SQLException e) {
			throw e;
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					;
				}
				result = null;
			}
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
	}

	public Collection<String> showDbs() throws SQLException {
		Collection<String> dbs = new ArrayList<>();

		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			conn = dataSource.getConnection();
			String sql = "SHOW DATABASES";
			stat = conn.prepareStatement(sql);
			result = stat.executeQuery();
			while (result.next()) {
				dbs.add(result.getString("database_name"));
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					;
				}
				result = null;
			}
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
		return dbs;
	}

	public Collection<String> showTables(String db) throws SQLException {
		Collection<String> tables = new ArrayList<>();

		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			conn = dataSource.getConnection();
			String sql = "SHOW TABLES IN " + db;
			stat = conn.prepareStatement(sql);
			result = stat.executeQuery();
			while (result.next()) {
				tables.add(result.getString("tab_name"));
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					;
				}
				result = null;
			}
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
		return tables;
	}

	public void createDB(String db) throws SQLException {
		Connection conn = null;
		Statement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "create database if not exists " + db;
			stat = conn.createStatement();
			stat.execute(sql);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
	}

	public void removeDB(String db) throws SQLException {
		Connection conn = null;
		Statement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "drop database if exists " + db;
			stat = conn.createStatement();
			stat.execute(sql);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
	}

	public void dropTable(TableMeta tableMeta) {
		Connection conn = null;
		Statement stat = null;
		try {
			conn = dataSource.getConnection();
			stat = conn.createStatement();
			StringBuilder builder = new StringBuilder();
			String db = tableMeta.getDbName();
			String table = tableMeta.getName();
			if (StringUtils.isNotEmpty(db) && StringUtils.isNotEmpty(table)) {
				builder.append("DROP TABLE IF EXISTS ").append(db).append(".").append(table);
				stat.execute(builder.toString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					;
				}
				stat = null;
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}
		}
	}

}