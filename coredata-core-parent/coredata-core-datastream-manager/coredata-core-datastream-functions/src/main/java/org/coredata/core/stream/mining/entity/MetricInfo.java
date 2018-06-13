package org.coredata.core.stream.mining.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.coredata.core.model.mining.Param;
import org.coredata.core.stream.vo.DSInfo;
import org.coredata.core.stream.vo.TransformData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.googlecode.aviator.AviatorEvaluator;

public class MetricInfo implements Delayed, Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2115804208727636234L;

	private static final Logger logger = LoggerFactory.getLogger(MetricInfo.class);

	private static final String METRIC_ID = "metricId";

	private static final String INSTANCE_ID = "instId";

	private static final String TASK_TIME = "tasktime";

	private static final String P_KEY = "pkeys";

	private static final String CMD_NAME = "cmdId";

	private String id;

	private String instId;

	private String expression;

	private DSInfo[] dsinfo;

	private String type;

	private Map<String, TransformData> dataCache = new HashMap<>();

	private Map<String, String> aliasToCmdName = new HashMap<>();

	private AtomicInteger counter = null;

	private long tasktime = 0;

	private List<Param> params = new ArrayList<>();

	//Delayd

	//default expired delay time 30min
	private long delay = 30 * 60 * 1000;

	private long createdTime = System.currentTimeMillis();

	private long expire = createdTime + delay;

	/**
	 * 是否需要四舍五入保留小数
	 */
	private boolean needScale = false;

	public MetricInfo() {

	}

	public MetricInfo(String id, String instId, String expression, String type, DSInfo[] dsinfo) {
		this.id = id;
		this.instId = instId;
		this.expression = expression;
		this.dsinfo = dsinfo;
		this.type = type;
		for (DSInfo dsInfo : dsinfo) {
			dataCache.put(dsInfo.getAlias(), null);
		}
		counter = new AtomicInteger(dataCache.size());
	}

	public boolean readyToMining() {
		return counter.get() == 0;
	}

	public void reProcessDsInfo(List<DSInfo> dsinfos) {
		dataCache.clear();
		this.dsinfo = dsinfos.toArray(new DSInfo[dsinfos.size()]);
		for (DSInfo dsInfo : dsinfos) {
			dataCache.put(dsInfo.getAlias(), null);
		}
		counter = new AtomicInteger(dataCache.size());
	}

	public Object mining() {

		Map<String, Object> param = new HashMap<>();
		param.put(METRIC_ID, this.getId());
		param.put(INSTANCE_ID, this.getInstId());
		param.put(TASK_TIME, this.getTasktime());
		//替换表达式中变量
		replaceExp(params, param, dsinfo);
		for (Entry<String, TransformData> entry : dataCache.entrySet()) {
			if (entry.getValue().isError() && StringUtils.isEmpty(entry.getValue().getResult())) {
				return null;
			}
		}
		try {
			return AviatorEvaluator.execute(expression, param, true);
		} catch (Throwable e) {
			logger.error("Expression Error:metric is " + this.getId() + ";expression is " + expression, e);
			return null;
		}
	}

	private void replaceExp(List<Param> params, Map<String, Object> param, DSInfo[] dsinfo) {
		Map<String, String> pkeys = new HashMap<>();
		Map<String, String> aliasCmd = new HashMap<>();
		for (Param p : params) {
			String value = p.getValue();
			for (DSInfo ds : dsinfo) {
				if (value.equals(ds.getId())) {
					param.put(p.getKey(), ds.getValue());
					pkeys.put(p.getKey(), ds.getKey());
					aliasCmd.put(p.getKey(), aliasToCmdName.get(ds.getAlias()));
				}
			}
		}
		param.put(P_KEY, pkeys);
		param.put(CMD_NAME, aliasCmd);
	}

	public boolean needBinding(String alias, String instId) {
		return dataCache.containsKey(alias) && instId.endsWith(this.instId);
	}

	public boolean cacheData(String alias, TransformData data) {
		if (dataCache.get(alias) == null && dataCache.containsKey(alias)) {
			dataCache.put(alias, data);
			if (counter.decrementAndGet() == 0) {
				return true;
			}
		} else if (counter.get() == 0) {
			return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public DSInfo[] getDsinfo() {
		return dsinfo;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "MetricInfo [id=" + id + ", expression=" + expression + ", dsinfo=" + Arrays.toString(dsinfo) + ", type=" + type + ", dataCache=" + dataCache
				+ ", counter=" + counter.get() + "]";
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(this.expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
	}

	public boolean isNeedScale() {
		return needScale;
	}

	public void setNeedScale(boolean needScale) {
		this.needScale = needScale;
	}

	public String getCacheKey() {
		return "metric_" + id;
	}

	public String getInstId() {
		return instId;
	}

	public long getTasktime() {
		return tasktime;
	}

	public void setTasktime(long tasktime) {
		this.tasktime = tasktime;
	}

	public List<Param> getParams() {
		return params;
	}

	public void setParams(List<Param> params) {
		this.params = params;
	}

	public Map<String, String> getAliasToCmdName() {
		return aliasToCmdName;
	}

	public void setAliasToCmdName(Map<String, String> aliasToCmdName) {
		this.aliasToCmdName = aliasToCmdName;
	}

	/**
	 * @return the dataCache
	 */
	public Map<String, TransformData> getDataCache() {
		return dataCache;
	}

	/**
	 * @param dataCache the dataCache to set
	 */
	public void setDataCache(Map<String, TransformData> dataCache) {
		this.dataCache = dataCache;
	}

	/**
	 * @return the counter
	 */
	public AtomicInteger getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(AtomicInteger counter) {
		this.counter = counter;
	}

	/**
	 * @return the delay
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * @return the createdTime
	 */
	public long getCreatedTime() {
		return createdTime;
	}

	/**
	 * @param createdTime the createdTime to set
	 */
	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	/**
	 * @return the expire
	 */
	public long getExpire() {
		return expire;
	}

	/**
	 * @param expire the expire to set
	 */
	public void setExpire(long expire) {
		this.expire = expire;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param instId the instId to set
	 */
	public void setInstId(String instId) {
		this.instId = instId;
	}

	/**
	 * @param dsinfo the dsinfo to set
	 */
	public void setDsinfo(DSInfo[] dsinfo) {
		this.dsinfo = dsinfo;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}