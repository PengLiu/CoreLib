package org.coredata.core.stream.decision;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.coredata.core.entities.ResEntity;
import org.coredata.core.metric.documents.Metric;
import org.coredata.core.model.decision.DecisionModel;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class SysDecisionModelFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static Logger logger = LoggerFactory.getLogger(SysDecisionModelFlow.class);

	public final Inlet<A> in = Inlet.create("SysDecisionModelFlow.in");

	public final Outlet<B> out = Outlet.create("SysDecisionModelFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private RedisService redisService = null;

	private ObjectMapper mapper = new ObjectMapper();

	private final String BUSINESS_PREFIX = "business_";

	private ConcurrentMap<String, DecisionModel> modelCache = new ConcurrentHashMap<String, DecisionModel>();

	public SysDecisionModelFlow(RedisService redisService, String topic) {
		this.redisService = redisService;
		//注册决策模型订阅消息
		ChannelTopic channel = new ChannelTopic(topic);
		redisService.registSubscriber(new MessageListener() {
			@Override
			public void onMessage(Message message, byte[] pattern) {
				try {
					DecisionModel model = mapper.readValue(message.getBody(), DecisionModel.class);
					if (logger.isDebugEnabled())
						logger.debug("Received change decision model event :  " + model);
					modelCache.put(model.getId(), model);
					redisService.saveData(RedisService.DECISION, model.getId(), model);
				} catch (Exception e) {
					logger.error("Subscrib transform model error.", e);
				}
			}
		}, channel);
	}

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	private String findDecisionModel(String instId) {
		//TODO 未来需要协商业务资产是否需要单独存入redis表中
		if (instId.indexOf(BUSINESS_PREFIX) != -1) {
			ResEntity business = (ResEntity) redisService.loadDataByTableAndKey(RedisService.BUSINESS, instId);
			if (business != null) {
				Map<String, Object> props = business.getProps();
				Object model = props.get("decisionId");
				return model == null ? null : model.toString();
			}
		} else {
			ResEntity inst = (ResEntity) redisService.loadDataByTableAndKey(RedisService.INSTANCE, instId);
			if (inst != null) {
				Map<String, Object> props = inst.getProps();
				Object model = props.get("decisionId");
				return model == null ? null : model.toString();
			}
		}
		return null;
	}

	public DecisionModel syncModelById(String modelId) {
		DecisionModel model = modelCache.get(modelId);
		if (model != null)
			return model;
		try {
			model = (DecisionModel) redisService.loadDataByTableAndKey(RedisService.DECISION, modelId);
			modelCache.put(modelId, model);
		} catch (Exception e) {
			logger.error("Load model " + modelId + " error", e);
		}
		return model;
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
						for (Metric metric : metrics) {
							if (logger.isDebugEnabled())
								logger.debug("Start decision from TSDBMetric is:" + JSON.toJSONString(metric));
							String mid = findDecisionModel(metric.getEntityId());
							if (mid != null) {
								DecisionModel model = syncModelById(mid);//获取挖掘模型
								metric.setDecisionModel(mapper.writeValueAsString(model));
							}
						}
						push(out, (B) metrics);
					}
				});

				setHandler(out, new AbstractOutHandler() {
					@Override
					public void onPull() throws Exception {
						pull(in);
					}
				});
			}
		};
	}

}
