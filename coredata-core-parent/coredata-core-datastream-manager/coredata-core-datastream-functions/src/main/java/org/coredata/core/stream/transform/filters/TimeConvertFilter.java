package org.coredata.core.stream.transform.filters;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.vo.TransformData;
import org.coredata.core.util.common.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TimeConvertFilter extends AbsFilter {

	private static final long serialVersionUID = -3077138164813252021L;
	private static String ruleExp = "time_convert\\(\"\\$\\{(.*?)\\}\"\\)";
	private static Pattern p = Pattern.compile(ruleExp);

	private static final Pattern PATTERN = Pattern.compile("(\\d+[a-zA-Z0-9])+");
	private String filterRule;

	public TimeConvertFilter() {

	}

	public TimeConvertFilter(String filterRule) {
		this.filterRule = filterRule;
	}

	@Override
	public void doFilter(TransformData response, FilterChain chain) {

		JsonNode json = response.getResultJson();
		Matcher m = p.matcher(filterRule);
		if (m.find() && m.groupCount() == 1) {
			String key = m.group(1);
			JsonNode valNode = json.get(key);
			if (valNode != null) {
				long period = 0l;
				Matcher matcher = PATTERN.matcher(valNode.asText().replaceAll("\\s", ""));
				while (matcher.find()) {
					period += Duration.parseDuration(matcher.group()).convert(TimeUnit.MILLISECONDS);
				}
				((ObjectNode) json).put(key, period);
			}
		}
		chain.doFilter(response);

	}

	public static String subStr(String text, String start, String end, boolean withFix) {
		int startIndex = text.indexOf(start);
		int endIndex = text.indexOf(end);
		if (withFix) {
			return text.substring(startIndex, endIndex + end.length());
		} else {
			return text.substring(startIndex + start.length(), endIndex);
		}
	}

}
