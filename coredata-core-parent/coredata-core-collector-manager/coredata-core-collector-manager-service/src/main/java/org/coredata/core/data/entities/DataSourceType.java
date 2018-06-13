package org.coredata.core.data.entities;

import java.util.HashMap;
import java.util.Map;

public enum DataSourceType {

	csvReader, excelReader, jdbcReader, httpReader, kafkaReader, syslogReader, binaryReader, crawlerReader, esWriter, hdfsWriter;

	private static final Map<String, DataSourceType> stringToEnum = new HashMap<String, DataSourceType>();
	private static final Map<String, Integer> stringToInteger = new HashMap<String, Integer>();

	static {
		for (DataSourceType type : values()) {
			stringToEnum.put(type.toString(), type);
		}
		DataSourceType[] types = values();
		for (int i = 0; i < types.length; i++) {
			DataSourceType type = types[i];
			stringToInteger.put(type.toString(), i);

		}
	}

	// Returns Blah for string, or null if string is invalid
	public static DataSourceType fromString(String symbol) {
		return stringToEnum.get(symbol);
	}

	// Returns Blah for string, or null if string is invalid
	public static int fromInteger(String symbol) {
		return stringToInteger.get(symbol);
	}
}
