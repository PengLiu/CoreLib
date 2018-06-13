package org.coredata.core.data.readers.csv;

import org.apache.commons.csv.CSVFormat;

/**
 * Created by dog on 4/15/17.
 */
final public class FormatConf {

	public static CSVFormat confCsvFormat(String format) {
		CSVFormat csvFormat = CSVFormat.DEFAULT;
		if (format != null) {
			switch (format) {
			case "default":
				csvFormat = CSVFormat.DEFAULT;
				break;
			case "excel":
				csvFormat = CSVFormat.EXCEL;
				break;
			case "mysql":
				csvFormat = CSVFormat.MYSQL;
				break;
			case "tdf":
				csvFormat = CSVFormat.TDF;
				break;
			case "rfc4180":
				csvFormat = CSVFormat.RFC4180;
				break;
			default:
				csvFormat = CSVFormat.DEFAULT;
				break;
			}
		}
		return csvFormat;
	}
}
