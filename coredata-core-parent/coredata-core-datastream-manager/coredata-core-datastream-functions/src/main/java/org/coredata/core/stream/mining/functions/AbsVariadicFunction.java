package org.coredata.core.stream.mining.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;

public abstract class AbsVariadicFunction extends AbstractVariadicFunction {

	final Logger logger = LoggerFactory.getLogger(getClass());

	public static final String METRIC_INFO = "MetricInfo";

	public static final String TRANSFORM_DATA = "TransformData";

	void logError(Throwable e, MetricInfo metricInfo) {
		logger.error(getName() + " error " + e.getMessage() + ":" + metricInfo);
	}

	void logError(Throwable e) {
		logger.error(getName() + " error " + e.getMessage());
	}

	public static final String strExp = "\\{(.*?)\\}";
	public static final Pattern strPattern = Pattern.compile(strExp);

	/**
	* 该方法用于抽取条件表达式中条件部分的id
	* @param condition
	* @return
	*/
	protected Map<String, String> extractionConditionIds(String condition, Pattern pattern, int i) {
		Map<String, String> is = new HashMap<>();
		Matcher dsMatcher = pattern.matcher(condition);
		while (dsMatcher.find()) {
			is.put(dsMatcher.group(), dsMatcher.group(i));
		}
		return is;
	}

	public String trimFirstAndLastChar(String source, String string) {
		boolean beginIndexFlag = true;
		boolean endIndexFlag = true;
		do {
			int beginIndex = source.indexOf(string) == 0 ? 1 : 0;
			int endIndex = source.lastIndexOf(string) + 1 == source.length() ? source.lastIndexOf(string) : source.length();
			source = source.substring(beginIndex, endIndex);
			beginIndexFlag = (source.indexOf(string) == 0);
			endIndexFlag = (source.lastIndexOf(string) + 1 == source.length());
		} while (beginIndexFlag || endIndexFlag);
		return source;
	}
}
