package org.coredata.core.data.readers.jdbc;

public class MySQLBuilder implements ISQLBuilder{

	@Override
	public String  buildShowDbsSql() {
		return "SHOW DATABASES";
	}

	@Override
	public String  buildShowTablesSql(String db) {
		return new StringBuffer().append("SELECT table_name FROM information_schema.TABLES WHERE TABLE_SCHEMA='").append(db).append("'").toString();
	}

	@Override
	public String buildShowColumnsSql(String db, String tableName) {
		return new StringBuffer().append("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name = '").append(tableName)
				.append("' AND table_schema = '").append(db).append("'").toString();
	}


}
