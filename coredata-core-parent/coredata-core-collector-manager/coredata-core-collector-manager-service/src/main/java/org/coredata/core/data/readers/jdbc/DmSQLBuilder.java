package org.coredata.core.data.readers.jdbc;

public class DmSQLBuilder implements ISQLBuilder {

	@Override
	public String buildShowDbsSql() {
		return "select name from v$database";
	}

	@Override
	public String buildShowTablesSql(String db) {
		return "select table_name from user_all_tables";
	}

	@Override
	public String buildShowColumnsSql(String db, String tableName) {
		return new StringBuffer().append("select column_name FROM user_tab_columns where table_name='")
				.append(tableName).append("'").toString();
	}

}
