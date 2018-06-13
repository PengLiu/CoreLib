package org.coredata.core.data.readers.jdbc;

public class SQLServerBuilder implements ISQLBuilder {

	@Override
	public String buildShowDbsSql() {
		return "select name from sysdatabases";
	}

	@Override
	public String buildShowTablesSql(String db) {
		return "select name from sysobjects where xtype='U'";
	}

	@Override
	public String buildShowColumnsSql(String db, String tableName) {
		return new StringBuffer().append("select name from syscolumns where id =Object_id('").append(tableName)
				.append("')").toString();
	}

}
