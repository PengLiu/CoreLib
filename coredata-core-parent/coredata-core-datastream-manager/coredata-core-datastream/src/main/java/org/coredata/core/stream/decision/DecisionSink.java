package org.coredata.core.stream.decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.coredata.core.model.decision.Action;
import org.coredata.core.model.decision.Associatedres;
import org.coredata.core.model.decision.Decision;
import org.coredata.core.model.decision.DecisionModel;
import org.coredata.core.model.decision.DecisionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;

import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import akka.stream.Attributes;
import akka.stream.Inlet;
import akka.stream.SinkShape;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class DecisionSink<A> extends GraphStage<SinkShape<A>> {

	private static Logger logger = LoggerFactory.getLogger(DecisionSink.class);

	public final Inlet<A> in = Inlet.create("DecisionSink.in");

	private final SinkShape<A> shape = SinkShape.of(in);

	private Map<String, DelayQueue<DecisionProcessor>> rules = new HashMap<>();

	private KafkaProducer<String, String> producer;

	private String alarmTopic;

	private MetricService metricService;

	public DecisionSink(String kafkaAddr, ActorSystem system, int parallelNum, String alarmTopic, MetricService metricService) {
		ProducerSettings<String, String> producerSettings = ProducerSettings.create(system, new StringSerializer(), new StringSerializer())
				.withBootstrapServers(kafkaAddr).withParallelism(parallelNum);
		this.producer = producerSettings.createKafkaProducer();
		this.alarmTopic = alarmTopic;
		this.metricService = metricService;
	}

	@Override
	public SinkShape<A> shape() {
		return shape;
	}

	private void processCache(Metric metric, String mid) {
		DelayQueue<DecisionProcessor> ruleses = rules.get(mid);
		if (ruleses == null)
			return;
		for (DecisionProcessor rule : ruleses) {
			int counter = rule.process(metric);
			if (counter == 0) {
				ruleses.remove(rule);
				rules.put(mid, ruleses);
				rule.calExp(producer, alarmTopic);
			}
		}
	}

	private boolean needMetric(Decision decision, Metric metric) {
		Associatedres associatedres = decision.getAssociatedres();
		List<String> metrics = Stream.of(associatedres.getMetric()).collect(Collectors.toList());
		List<String> entityIds = Stream.of(associatedres.getInstid()).collect(Collectors.toList());
		if (!metrics.contains(metric.getMetricId()))
			return false;
		if (entityIds.contains("?") || entityIds.contains("*") || entityIds.contains(metric.getEntityId()))
			return true;
		return false;
	}

	@Override
	public GraphStageLogic createLogic(Attributes attr) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {
					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						List<Metric> metrics = (List<Metric>) grab(in);
						try {
							for (Metric metric : metrics) {
								String decisionModel = metric.getDecisionModel();
								DecisionModel model = JSON.parseObject(decisionModel, DecisionModel.class);
								List<Decision> models = model.getDecision();
								if (CollectionUtils.isEmpty(models))
									continue;
								processCache(metric, model.getId());
								for (Decision decision : models) {
									if (!needMetric(decision, metric))
										continue;
									//重新定义逻辑，考虑状态更新
									boolean hasStateChange = false;
									if (!Boolean.TRUE.equals(decision.getEnable())) {
										List<DecisionRule> rule = decision.getRule();
										for (DecisionRule r : rule) {
											Optional<Action> stateAction = r.getAction().stream().filter(a -> "stateTransition".equals(a.getType())).findAny();
											if (!stateAction.isPresent())
												continue;
											hasStateChange = true;
											break;
										}
										if (!hasStateChange)
											continue;
									}
									for (DecisionRule rule : decision.getRule()) {
										if (!rule.isEnable() && !hasStateChange)
											continue;
										List<String> ms = Stream.of(decision.getAssociatedres().getMetric()).collect(Collectors.toList());
										DecisionProcessor dr = new DecisionProcessor(
												decision.getEnable().equals(rule.isEnable()) ? Boolean.toString(rule.isEnable())
														: Boolean.toString(decision.getEnable()),
												rule.getExp(), rule.getAction(), ms, metricService);
										if (dr.process(metric) == 0) {
											dr.calExp(producer, alarmTopic);
										} else {
											DelayQueue<DecisionProcessor> queue = rules.get(model.getId());
											if (queue == null)
												queue = new DelayQueue<>();
											queue.add(dr);
											rules.put(model.getId(), queue);
										}
									}
								}
							}
						} catch (Exception e) {
							logger.error("Save metrics error.", e);
						} finally {
							pull(in);
						}
					}
				});
			}

			@Override
			public void preStart() {
				pull(in);
			}
		};
	}

}
