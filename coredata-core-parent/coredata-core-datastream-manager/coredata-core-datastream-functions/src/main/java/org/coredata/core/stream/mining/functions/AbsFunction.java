package org.coredata.core.stream.mining.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.util.SpringUtil;
import org.coredata.core.stream.vo.TransformData;
import org.coredata.core.stream.vo.Unit;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.aviator.runtime.function.AbstractFunction;

public abstract class AbsFunction extends AbstractFunction {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected static RedisService redisService = SpringUtil.getBean(RedisService.class);

	protected ObjectMapper mapper = new ObjectMapper();

	public static final String METRIC_INFO = "MetricInfo";

	public static final String TRANSFORM_DATA = "TransformData";

	public static final String METRIC_ID = "metricId";

	public static final String INSTANCE_ID = "instId";

	public static final String TASK_TIME = "tasktime";

	public static final String CMD_NAME = "cmdId";

	public static final String P_KEY = "pkeys";

	public static final String CACHE = "cache";

	public static final int HEX_SYS = 16;

	void logError(Throwable e, MetricInfo metricInfo) {
		logger.error(getName() + " error " + e.getMessage() + ":" + metricInfo);
	}

	void logError(Throwable e) {
		logger.error(getName() + " error " + e.getMessage());
	}

	public static final String strExp = "\\$\\{(.*?)\\}(\\.\\$\\{(.*?)\\})+";
	public static final Pattern strPattern = Pattern.compile(strExp);

	public static final String envExp = "\\{(.*?)\\}";
	public static final Pattern envPattern = Pattern.compile(envExp);

	//组合数据源匹配
	public static final String mdExp = "\\$\\{MD}(\\.\\$\\{(.*?)\\})+";
	public static final Pattern mdPattern = Pattern.compile(mdExp);

	//组合结果集匹配
	public static final String mdrExp = "\\$\\{MDR}(\\.\\$\\{(.*?)\\})+";
	public static final Pattern mdrPattern = Pattern.compile(mdrExp);

	public static final String matchStrExp = "\\{(.*?)\\}";
	public static final Pattern matchStrPattern = Pattern.compile(matchStrExp);

	/**
	* 该方法用于抽取条件表达式中条件部分的id
	* @param condition
	* @return
	*/
	public static Map<String, String> extractionConditionIds(String condition) {
		Map<String, String> is = new HashMap<>();
		Matcher dsMatcher = strPattern.matcher(condition);
		while (dsMatcher.find()) {
			is.put(dsMatcher.group(), dsMatcher.group(3));
		}
		return is;
	}

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

	/**
	* 该方法用于获取表达式中的字段名
	* @param
	* @return
	*/
	public String getField(String rtindex, int i) {
		String field = null;
		Matcher matcher = strPattern.matcher(rtindex);
		if (matcher.find()) {
			field = matcher.group(i);
		}
		return field;
	}

	/**
	* 该方法用于去除两边字符
	* @param
	* @return
	*/
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

	/**
	 * 该方法用于抽取期望值部分变量内容
	 * @param cmd
	 * @return
	 */
	public Map<String, String> extractionInstance(String cmd) {
		Map<String, String> excepts = new HashMap<>();
		Matcher dsMatcher = strPattern.matcher(cmd);
		while (dsMatcher.find()) {
			String key = dsMatcher.group(1);
			String value = dsMatcher.group(3);
			excepts.put(key, value);
		}
		return excepts;
	}

	protected Map<String, TransformData> changeTransformData(Map<String, TransformData> data) {
		Map<String, TransformData> results = new HashMap<>();
		Set<Entry<String, TransformData>> datas = data.entrySet();
		for (Entry<String, TransformData> entry : datas) {
			String alias = entry.getKey();
			TransformData transformData = entry.getValue();
			TransformData newData = new TransformData();
			newData.setCustomerId(transformData.getCustomerId());
			newData.setErrMsg(transformData.getErrMsg());
			newData.setError(transformData.isError());
			newData.setFinishTime(transformData.getFinishTime());
			newData.setInstanceId(transformData.getInstanceId());
			newData.setModelid(transformData.getModelid());
			newData.setName(transformData.getName());
			newData.setNodeId(transformData.getNodeId());
			newData.setParams(transformData.getParams());
			newData.setResult(transformData.getResult());
			newData.setTasktime(transformData.getTasktime());
			newData.setType(transformData.getType());
			results.put(alias, newData);
		}
		return results;

	}

	@SuppressWarnings("unchecked")
	protected Unit getOldUnit(String instId, String cmd, String key, String metricId) {
		String rkey = CACHE + ":" + instId + ":" + cmd;
		Map<String, Object> unitCache = (Map<String, Object>) redisService.loadDataByTableAndKey(RedisService.CMDCACHE, rkey);
		if (unitCache == null)
			return null;
		Object units = unitCache.get(metricId);
		if (units == null)
			return null;
		Map<String, Unit> values = (Map<String, Unit>) units;
		Unit results = values.get(key);
		if (results == null)
			return null;
		return results;
	}

	@SuppressWarnings("unchecked")
	protected void saveNewUnit(String instId, String cmd, String key, String metricId, Unit unit) {
		String rkey = CACHE + ":" + instId + ":" + cmd;
		Map<String, Object> unitCache = (Map<String, Object>) redisService.loadDataByTableAndKey(RedisService.CMDCACHE, rkey);
		if (unitCache == null)
			unitCache = new HashMap<>();
		Map<String, Unit> uCache = (Map<String, Unit>) unitCache.get(metricId);
		if (uCache == null) {
			uCache = new HashMap<>();
		}
		uCache.put(key, unit);
		unitCache.put(metricId, uCache);
		redisService.saveData(RedisService.CMDCACHE, rkey, unitCache);
	}

}