package org.coredata.core.stream.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.model.decision.Action;

/**
 * 告警对象，目的为了将告警数据存入kafka
 * @author sue
 *
 */
public class AlarmData implements Serializable {

	private static final long serialVersionUID = 8894666509359909085L;

	/**
	 * 告警表达式运行结果
	 */
	private boolean alarmResult;

	/**
	 * 告警动作
	 */
	private List<Action> actions = new ArrayList<>();

	/**
	 * 告警资产
	 */
	private List<String> instances = new ArrayList<>();

	/**
	 * 告警规则id
	 */
	private String ruleId;

	/**
	 * 告警规则表达式
	 */
	private String expConditionStr;

	/**
	 * 告警规则是否可用，用于资产状态改变
	 */
	private String enable = "true";

	/**
	 * 指标名称
	 */
	private List<String> metric = new ArrayList<>();

	/**
	 * 指标值
	 */
	private Map<String, Object> metricValue = new HashMap<>();

	public AlarmData() {

	}

	public AlarmData(boolean alarmResult, List<Action> actions, List<String> instances, String ruleId, String expConditionStr, List<String> metric,
			Map<String, Object> metricValue, String enable) {
		this.alarmResult = alarmResult;
		this.actions = actions;
		this.instances = instances;
		this.ruleId = ruleId;
		this.expConditionStr = expConditionStr;
		this.metric = metric;
		this.metricValue = metricValue;
		this.enable = enable;
	}

	public boolean getAlarmResult() {
		return alarmResult;
	}

	public void setAlarmResult(boolean alarmResult) {
		this.alarmResult = alarmResult;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	public List<String> getInstances() {
		return instances;
	}

	public void setInstances(List<String> instances) {
		this.instances = instances;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getExpConditionStr() {
		return expConditionStr;
	}

	public void setExpConditionStr(String expConditionStr) {
		this.expConditionStr = expConditionStr;
	}

	public List<String> getMetric() {
		return metric;
	}

	public void setMetric(List<String> metric) {
		this.metric = metric;
	}

	public Map<String, Object> getMetricValue() {
		return metricValue;
	}

	public void setMetricValue(Map<String, Object> metricValue) {
		this.metricValue = metricValue;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

}
