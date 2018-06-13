package org.coredata.core.data.readers.jdbc;

public class SqlBuilderFactory {
	public static ISQLBuilder getBuilder(String type) {
		switch (type) {
		case "mysql":
			return new MySQLBuilder();
		case "oracle":
			return new OracleBuilder();
		case "sqlserver":
			return new SQLServerBuilder();
		case "dm":
			return new DmSQLBuilder();
		default:
			return null;
		}
	}
}
