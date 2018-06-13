package org.coredata.core.data.readers.jdbc;

public interface ISQLBuilder {

	public String buildShowDbsSql();
	public String  buildShowTablesSql(String db);
	public String  buildShowColumnsSql(String db, String tableName);
}
