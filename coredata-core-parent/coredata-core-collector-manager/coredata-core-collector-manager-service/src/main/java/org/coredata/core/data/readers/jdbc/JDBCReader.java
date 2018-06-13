package org.coredata.core.data.readers.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.coredata.core.data.Constants;
import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.coredata.core.data.exception.DataException;
import org.coredata.core.data.util.NumberUtils;
import org.coredata.core.data.util.Utils;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

@Service(value = "jdbcReader")
@Scope("prototype")
public class JDBCReader extends Reader {

	public static final String CONDITIONS = "$CONDITIONS";
	private static final Pattern PATTERN = Pattern
			.compile("^([a-zA-Z]\\w*)(\\[(\\d+)-(\\d+)\\])?(_(\\d+)_(\\d+)_(\\d+))?$");

	private Connection connection;
	private String sqlQuery;
	private String url;
	private int columnCount;
	// private int sequence;
	// private JDBCIterator sqlPiece;
	private int[] columnTypes;
	private String nullString = null;
	private String nullNonString = null;
	private String fieldWrapReplaceString = null;
	private DecimalFormat decimalFormat = null;
	private long sqlMetricTime = -1;
	private String checkColumn;
	private int checkIndex;
	private String lastVal;
	private int maxFetchSize;
	private Record currentRecord;
	private long index = 1L;
	private static final Logger LOGGER = LogManager.getLogger("sql-metric");

	@Override
	public void prepare(PluginConfig readerConfig) {
		super.prepare(readerConfig);
		// sqlMetricTime =
		// context.getEngineConfig().getLong(HDataConfigConstants.JDBC_READER_SQL_METRIC_TIME_MS,
		// -1);
		String driver = readerConfig.getString(JDBCReaderProperties.DRIVER);
		url = readerConfig.getString(JDBCReaderProperties.URL);
		if (!StringUtils.isEmpty(url) && url.indexOf("jdbc:mysql") != -1 && url.indexOf("useSSL=false") == -1) {
			if (url.endsWith("/") || url.endsWith("\\")) {
				url = url.substring(0, url.length() - 1) + "?useSSL=false";
			} else {
				url = url + "?useSSL=false";
			}
		}
		String username = readerConfig.getString(JDBCReaderProperties.USERNAME);
		String password = readerConfig.getString(JDBCReaderProperties.PASSWORD);
		nullString = readerConfig.getString(JDBCReaderProperties.NULL_STRING);
		nullNonString = readerConfig.getString(JDBCReaderProperties.NULL_NON_STRING);
		fieldWrapReplaceString = readerConfig.getProperty(JDBCReaderProperties.FIELD_WRAP_REPLACE_STRING);

		// String keywordEscaper =
		// readerConfig.getProperty(JDBCReaderProperties.KEYWORD_ESCAPER, "");
		maxFetchSize = readerConfig.getInt(JDBCReaderProperties.MAX_SIZE_PER_FETCH, 0);
		lastVal = readerConfig.getString(JDBCReaderProperties.LAST_VAL, "");
		checkColumn = readerConfig.getString(JDBCReaderProperties.CHECK_COLUMN, "");
		checkIndex = readerConfig.getInt(JDBCReaderProperties.CHECK_INDEX, -1);
		String numberFormat = readerConfig.getProperty(JDBCReaderProperties.NUMBER_FORMAT);
		if (numberFormat != null) {
			decimalFormat = new DecimalFormat(numberFormat);
		}

		String keywordEscaper = readerConfig.getProperty(JDBCReaderProperties.KEYWORD_ESCAPER, "");

		List<PluginConfig> readerConfigList = new ArrayList<PluginConfig>();

		if (readerConfig.containsKey(JDBCReaderProperties.SQL)
				&& !StringUtils.isEmpty(readerConfig.getString(JDBCReaderProperties.SQL))) {
			String sql = readerConfig.getString(JDBCReaderProperties.SQL);
			readerConfig.put(JDBCReaderProperties.SQLQuery, sql);
			readerConfigList.add(readerConfig);
		} else if (readerConfig.containsKey(JDBCReaderProperties.TABLE)
				&& !StringUtils.isEmpty(readerConfig.getString(JDBCReaderProperties.TABLE))) {
			String table = readerConfig.getString(JDBCReaderProperties.TABLE);
			Preconditions.checkNotNull(table, "JDBC reader required property: table");
			if (!isMatch(table)) {
				throw new DataException("table:" + table + " 格式错误");
			}
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ");
			if (!readerConfig.containsKey(JDBCReaderProperties.COLUMNS)
					&& !readerConfig.containsKey(JDBCReaderProperties.EXCLUDE_COLUMNS)) {
				sql.append("*");
			} else if (readerConfig.containsKey(JDBCReaderProperties.COLUMNS)) {
				String columns = readerConfig.getString(JDBCReaderProperties.COLUMNS);
				sql.append(columns);
			} else if (readerConfig.containsKey(JDBCReaderProperties.EXCLUDE_COLUMNS)) {
				String[] excludeColumns = readerConfig.getString(JDBCReaderProperties.EXCLUDE_COLUMNS).trim()
						.split(Constants.COLUMNS_SPLIT_REGEX);
				Connection conn;
				try {
					conn = JdbcUtils.getConnection(driver, url, username, password);
					String selectColumns = keywordEscaper
							+ Joiner.on(keywordEscaper + ", " + keywordEscaper).join(Utils
									.getColumns(JdbcUtils.getColumnNames(conn, table, keywordEscaper), excludeColumns))
							+ keywordEscaper;
					sql.append(selectColumns);
				} catch (Exception e) {
					throw new DataException(e);
				}
			}
			sql.append(" FROM ");
			sql.append(keywordEscaper).append(table).append(keywordEscaper);
			readerConfig.put(JDBCReaderProperties.SQLQuery, sql.toString());
			readerConfigList.add(readerConfig);
		} else {
			throw new DataException("no table no sql.");
		}

		try {
			connection = JdbcUtils.getConnection(driver, url, username, password);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			connection.setReadOnly(true);
		} catch (Exception e) {
			StringBuffer sb = new StringBuffer();
			sb.append("getConnection url:").append(this.url).append(" username:").append(username).append(" password:")
					.append(password).append(" driver:").append(driver);
			sb.append(" error:").append(e.getMessage());
			LOGGER.error(sb.toString());
			throw new DataException(e);
		}
		sqlQuery = readerConfig.getString(JDBCReaderProperties.SQLQuery);
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {
		try {
			if (!StringUtils.isEmpty(sqlQuery)) {
				boolean hasnext = true;
				while (hasnext) {
					StringBuffer sql = new StringBuffer();
					if (!StringUtils.isEmpty(lastVal) && !StringUtils.isEmpty(checkColumn) && checkIndex != -1) {
						sql.append("select * from (").append(sqlQuery).append(") tq  WHERE tq.").append(checkColumn)
								.append("> '").append(lastVal).append("'");

					} else {
						sql.append(sqlQuery);
					}
					hasnext = executeSingle(sql.toString(), recordCollector);
				}
				jobDetail.setStatus(JobStatus.Success);
			} else {
				jobDetail.setStatus(JobStatus.JobErr);
				throw new DataException("Sql is null.");
			}
		} catch (SQLException e) {
			jobDetail.setStatus(JobStatus.JobErr);
			throw new DataException(e);
		}

		return CompletableFuture.completedFuture(jobDetail);
	}

	private boolean executeSingle(String sql, RecordCollector recordCollector) throws SQLException {
		int rows = 0;
		boolean hasnext = false;
		boolean countLimit = false;
		long startTime = System.currentTimeMillis();
		long endTime = startTime;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			if (maxFetchSize > 0) {
				// statement.setFetchSize(maxFetchSize);
				statement.setMaxRows(maxFetchSize);
			}
			rs = statement.executeQuery();
			endTime = System.currentTimeMillis();
			if (columnCount == 0 || columnTypes == null) {
				ResultSetMetaData metaData = rs.getMetaData();
				columnCount = metaData.getColumnCount();
				columnTypes = new int[columnCount];
				for (int i = 1; i <= columnCount; i++) {
					addField(metaData.getColumnName(i));
					columnTypes[i - 1] = metaData.getColumnType(i);
				}
			}

			while (rs.next()) {
				Record r = new DefaultRecord(columnCount);
				for (int i = 1; i <= columnCount; i++) {
					Object o = rs.getObject(i);
					if (o == null && nullString != null && JdbcUtils.isStringType(columnTypes[i - 1])) {
						r.add(i - 1, nullString);
					} else if (o == null && nullNonString != null && !JdbcUtils.isStringType(columnTypes[i - 1])) {
						r.add(i - 1, nullNonString);
					} else if (o instanceof String && fieldWrapReplaceString != null) {
						r.add(i - 1, ((String) o).replace("\r\n", fieldWrapReplaceString).replace("\n",
								fieldWrapReplaceString));
					} else if (o instanceof oracle.sql.BLOB) {
						r.add(i - 1, ((oracle.sql.BLOB) o).getBytes());
					} else {
						if (decimalFormat != null) {
							if (o instanceof Double) {
								r.add(i - 1, Double.valueOf(decimalFormat.format(o)));
							} else if (o instanceof Float) {
								r.add(i - 1, Float.valueOf(decimalFormat.format(o)));
							} else {
								r.add(i - 1, o);
							}
						} else {
							r.add(i - 1, o);
						}
					}
				}
				doFilter(r);
				doSelect(r);
				currentRecord = r;
				recordCollector.send(r);
				rows++;
				if (getRecordLimit() > 0) {
					if (index >= getRecordLimit()) {
						index = 1L;
						countLimit = true;
						break;
					}
					index++;
				}
			}
			if (maxFetchSize > 0 && rows == maxFetchSize) {
				// 查询未完成,更新下次查询的条件
				if (!StringUtils.isEmpty(checkColumn) && checkIndex != -1) {
					lastVal = String.valueOf(currentRecord.get(checkIndex));
					hasnext = true;
				}
			}
		} catch (SQLException e) {
			Throwables.propagate(e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		long spendTime = endTime - startTime;
		if (sqlMetricTime > 0 && spendTime > sqlMetricTime) {
			LOGGER.info("time: {} ms, rows: {}, sql: {}, url: {}", spendTime, rows, sql, this.url);
		}
		return hasnext && !countLimit;
	}

	@Override
	public void close() {
		DbUtils.closeQuietly(connection);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(getFields());
	}

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public int[] getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(int[] columnTypes) {
		this.columnTypes = columnTypes;
	}

	/**
	 * 表名是否 符合要求
	 *
	 */
	public static Boolean isMatch(String content) {
		for (String piece : com.google.common.base.Splitter.on(",").omitEmptyStrings().trimResults().split(content)) {
			if (!PATTERN.matcher(piece).find()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 内容解析成列表
	 *
	 */
	public static List<String> getRange(String content) {
		// split to pieces and be unique.
		HashSet<String> hs = new HashSet<String>();
		for (String piece : com.google.common.base.Splitter.on(",").omitEmptyStrings().trimResults().split(content)) {
			hs.addAll(parseRange(piece));
		}

		List<String> range = new ArrayList<String>(hs);

		Collections.sort(range);

		return range;
	}

	/**
	 * get the range
	 *
	 * 01-04 = 01,02,03,04
	 *
	 */
	private static List<String> parseRange(String content) {
		Matcher matcher = PATTERN.matcher(content);
		List<String> pieces = new ArrayList<String>();

		if (!matcher.find()) {
			throw new RuntimeException(content + ": The format is wrong.");
		}

		if (!content.contains("[")) {
			pieces.add(content);
			return pieces;
		}

		String prefix = matcher.group(1);
		String begin = matcher.group(3);
		String after = matcher.group(4);

		String format = "%0" + begin.length() + "d";

		int[] rangeList = NumberUtils.getRange(Integer.valueOf(begin), Integer.valueOf(after));

		for (int number : rangeList) {
			String suffix = matcher.group(5);
			if (suffix != null) {
				pieces.add(prefix + String.format(format, number) + suffix);
			} else {
				pieces.add(prefix + String.format(format, number));
			}
		}

		return pieces;
	}

	public static String getFirstTableName(String tableName) {
		return getRange(tableName).get(0);
	}
}
