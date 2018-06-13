package org.coredata.core.stream.alarm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.model.decision.Action;
import org.coredata.core.model.decision.Flapping;
import org.coredata.core.model.decision.Param;
import org.coredata.core.util.common.Duration;
import org.springframework.util.StringUtils;

public class AlarmAction {

	private String alarmId;

	private long period;

	private Flapping flapping;

	private boolean expResult;

	private String exp;

	private List<Param> param = new ArrayList<>();

	private String level;

	private Collection<Long> slidingWindow = new ArrayList<>();

	private List<String> instances = new ArrayList<>();

	private List<String> metric = new ArrayList<>();

	private Map<String, Object> metricVal = new HashMap<>();

	public AlarmAction(String alarmId, boolean expResult, String exp, Action action, List<String> instances, List<String> metric,
			Map<String, Object> metricVal) {
		this.alarmId = alarmId;
		this.expResult = expResult;
		this.param = action.getParam();
		this.instances = instances;
		this.exp = exp;
		this.metric = metric;
		this.metricVal = metricVal;

		if (action.getPeriod() != null) {
			this.period = Duration.parseDuration(action.getPeriod()).toMilliseconds();
		} else {
			this.period = Duration.parseDuration("10m").toMilliseconds();
		}

		if (action.getFlapping() != null) {
			this.flapping = action.getFlapping();
		} else {
			this.flapping = new Flapping();
		}

		if (!StringUtils.isEmpty(action.getLevel())) {
			this.level = action.getLevel();
		} else {
			this.level = "3";
		}
	}

	public String getRelatedInstances() {
		if (instances.size() == 1) {
			return instances.get(0);
		} else {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < instances.size(); i++) {
				builder.append(instances.get(i));
				if (i < instances.size() - 1) {
					builder.append(",");
				}
			}
			return builder.toString();
		}
	}

	public boolean fire() {
		List<Long> removeItems = new ArrayList<>();
		long now = System.currentTimeMillis();
		slidingWindow.add(now);
		slidingWindow.forEach(time -> {
			if (System.currentTimeMillis() - time > this.period && this.period > 0) {
				removeItems.add(time);
			}
		});
		slidingWindow.removeAll(removeItems);
		if (flapping == null || slidingWindow.size() == Integer.valueOf(flapping.getCount())) {
			return true;
		}
		return false;
	}

	public boolean readyToAlarm() {
		switch (flapping.getType()) {
		case "consecutive":
			break;
		case "cumulative":
			break;
		}
		return false;
	}

	public int count() {
		return slidingWindow.size();
	}

	public void reset() {
		slidingWindow.clear();
	}

	public String getAlarmId() {
		return alarmId;
	}

	public boolean isExpResult() {
		return expResult;
	}

	public List<Param> getParam() {
		return param;
	}

	public String getLevel() {
		return level;
	}

	public Flapping getFlapping() {
		return flapping;
	}

	public String getExp() {
		return exp;
	}

	public List<String> getMetric() {
		return metric;
	}

	public Map<String, Object> getMetricVal() {
		return metricVal;
	}

}
