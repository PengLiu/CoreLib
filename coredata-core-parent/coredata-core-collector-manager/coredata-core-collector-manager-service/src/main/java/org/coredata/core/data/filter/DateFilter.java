package org.coredata.core.data.filter;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.Record;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.JsonNode;

public class DateFilter extends BaseFilter {

	private DateTimeFormatter istFormat = ISODateTimeFormat.dateTime();

	@Override
	public void doFilter(int columnIndex, Record record) {
		Locale locale = Locale.getDefault();
		try {
			Locale.setDefault(new Locale("en", "US"));
			String srcFormat = "";
			String targetFormat = "";
			if (StringUtils.isNoneBlank(actionRule)) {
				JsonNode json = mapper.readTree(actionRule);
				srcFormat = json.get("src").asText();
				targetFormat = json.get("target").asText();
			}
			if (!StringUtils.isEmpty(srcFormat) && !StringUtils.isEmpty(targetFormat)) {

				String time = String.valueOf(record.get(columnIndex));

				DateTime dateTime = null;

				switch (srcFormat) {
				case "CST":
					dateTime = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy").parseDateTime(time);
					break;
				case "UNIX_MS":
					dateTime = new DateTime(Long.valueOf(time));
					break;
				case "ISO8601":
					dateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").parseDateTime(time);
					break;
				default:
					dateTime = DateTimeFormat.forPattern(srcFormat).parseDateTime(time);
				}

				Object data = null;
				switch (targetFormat) {
				case "UNIX_MS":
					data = dateTime.getMillis();
					break;
				case "ISO8601":
					data = istFormat.print(dateTime);
					break;
				default:
					data = dateTime.toString(targetFormat);
				}

				processResult(data, columnIndex, record);
			}

		} catch (Throwable e) {
			;
		}finally {
			Locale.setDefault(locale);
		}

	}
}
