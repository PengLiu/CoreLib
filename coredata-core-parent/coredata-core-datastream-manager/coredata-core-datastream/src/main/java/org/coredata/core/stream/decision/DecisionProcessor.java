package org.coredata.core.stream.decision;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.model.decision.Action;
import org.coredata.core.stream.vo.AlarmData;
import org.coredata.util.query.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.aviator.AviatorEvaluator;

public class DecisionProcessor implements Delayed {

	private Logger logger = LoggerFactory.getLogger(DecisionProcessor.class);

	private static String exp = "\\$\\{(.*?)\\}(\\.\\$\\{(.*?)\\}(?:\\((.*?)\\))?)+";

	private static Pattern expPattern = Pattern.compile(exp);

	private ObjectMapper mapper = new ObjectMapper();

	private String ruleId;

	private String expConditionStr;

	private int counter;

	private String expStr;

	private String enable;

	private List<Action> actions = null;

	private List<String> instances = new ArrayList<>();

	private long delay = 30 * 60 * 1000;

	private long createdTime = System.currentTimeMillis();

	private long expire = createdTime + delay;

	private List<String> metric = new ArrayList<>();

	private Map<String, Object> metricValue = new HashMap<>();

	private MetricService metricService;

	public DecisionProcessor(String enable, String expStr, List<Action> actions, List<String> metric, MetricService metricService) {
		this(enable, expStr, 30 * 60 * 1000, actions, metric, metricService);
	}

	public DecisionProcessor(String enable, String expStr, long delay, List<Action> actions, List<String> metric, MetricService metricService) {
		this.enable = enable;
		this.expStr = expStr;
		this.ruleId = expStr;
		this.expConditionStr = expStr;
		this.delay = delay;
		this.actions = actions;
		this.metric = metric;
		this.metricService = metricService;
		count();
	}

	/**
	 * 计算实际所需的资源指标数据
	 */
	private void count() {
		Matcher matcher = expPattern.matcher(expStr);
		while (matcher.find()) {
			counter++;
		}
	}

	private void appendInstanceId(String instanceId) {
		instances.add(instanceId);
	}

	private void processExpression(Metric metric, String condition) {

		if (metric.getStringVal() != null) {
			expStr = expStr.replaceFirst(Pattern.quote(condition), "'" + metric.getStringVal() + "'");
			counter--;
		} else if (metric.getVal() != null) {
			expStr = expStr.replaceFirst(Pattern.quote(condition), metric.getVal().toString());
			counter--;
		} else if (metric.getBoolVal() != null) {
			expStr = expStr.replaceFirst(Pattern.quote(condition), metric.getBoolVal().toString());
			counter--;
		} else {
			expStr = expStr.replaceFirst(Pattern.quote(condition), "");
			counter--;
		}

	}

	private void process(Metric metric, String condition, String function) {

		if (StringUtils.isEmpty(function)) {
			processExpression(metric, condition);
		} else {

			try {
				switch (function) {
				case "last":
					String metricValueJson = loadMeticValue(metric.getEntityId(), metric.getMetricId(), metric.getCreatedTime());
					if (StringUtils.isEmpty(metricValueJson)) {
						break;
					}
					Metric tsdbMetric = mapper.readValue(metricValueJson, Metric.class);
					processExpression(tsdbMetric, condition);
					break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private String loadMeticValue(String instId, String metric, long time) {
		Pageable pageable = PageRequest.of(0, 1, Direction.DESC, "taskTime");
		TimeRange timeRange = new TimeRange(time - 600 * 1000, time);
		Page<Metric> pages = metricService.loadMetricByEntityAndTimeRange(instId, metric, timeRange, pageable);
		List<Metric> content = pages.getContent();
		if (CollectionUtils.isEmpty(content))
			return null;
		try {
			Metric m = content.get(0);
			return mapper.writeValueAsString(m);
		} catch (JsonProcessingException e) {
			logger.error("Load metric error.", e);
		}
		return null;
	}

	private void updateRuleId(String condition, String replace) {
		ruleId = ruleId.replace(condition, replace);
	}

	private void updateExpCondition(String condition, String replace) {
		expConditionStr = expConditionStr.replace(condition, replace);
	}

	public int process(Metric metric) {
		Matcher matcher = expPattern.matcher(expStr);
		while (matcher.find()) {
			String condition = matcher.group();
			String instanceId = matcher.group(1);
			String metricId = matcher.group(3);
			String function = matcher.group(4);
			if (metric.getMetricId().equals(metricId)) {
				//指标所属资源需要与Rule所属资源一致
				if ("?".equals(instanceId)) {
					if (CollectionUtils.isEmpty(instances)) {
						appendInstanceId(metric.getEntityId());
					}
					if (instances.contains(metric.getEntityId())) {
						process(metric, condition, function);
						//update rule id
						updateRuleId(condition, metric.getEntityId() + "." + metricId);
						updateExpCondition(condition, metricId);
						Object value = metric.getVal();
						if (metric.getBoolVal() != null) {
							value = metric.getBoolVal();
						}
						if (metric.getStringVal() != null) {
							value = metric.getStringVal();
						}

						metricValue.put(metricId, value);
					}
				}
			}
		}
		return counter;
	}

	public void calExp(KafkaProducer<String, String> producer, String topic) {
		try {
			boolean result = (Boolean) AviatorEvaluator.execute(this.expStr);
			//暂时屏蔽actions动作，将记录写入kafka
			AlarmData data = new AlarmData(result, this.actions, this.instances, this.ruleId, this.expConditionStr, this.metric, this.metricValue, this.enable);
			producer.send(new ProducerRecord<String, String>(topic, UUID.randomUUID().toString(), JSON.toJSONString(data)));
		} catch (Throwable e) {
			logger.error("Execute exp [" + this.expStr + "] error.");
		}
	}

	@Override
	public String toString() {
		return "DecisionRule [counter=" + counter + ", expStr=" + expStr + "]";
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(this.expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o) {
		return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
	}
}