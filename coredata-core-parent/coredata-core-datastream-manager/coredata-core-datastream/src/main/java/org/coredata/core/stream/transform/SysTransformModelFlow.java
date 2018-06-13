package org.coredata.core.stream.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.coredata.core.model.transform.TransformModel;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;

import com.fasterxml.jackson.databind.ObjectMapper;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

/**
 * 用于同步清洗模型flow
 * @author sue
 *
 */
public class SysTransformModelFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static final Logger logger = LoggerFactory.getLogger(SysTransformModelFlow.class);

	public final Inlet<A> in = Inlet.create("SysTransformModelFlow.in");

	public final Outlet<B> out = Outlet.create("SysTransformModelFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private ObjectMapper mapper = new ObjectMapper();

	private RedisService redisService;

	private ConcurrentMap<String, TransformModel> modelCache = new ConcurrentHashMap<String, TransformModel>();

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	public SysTransformModelFlow(RedisService redisService, String topic) {
		this.redisService = redisService;
		//注册清洗模型订阅消息
		ChannelTopic channel = new ChannelTopic(topic);
		redisService.registSubscriber(new MessageListener() {
			@Override
			public void onMessage(Message message, byte[] pattern) {
				try {
					TransformModel model = mapper.readValue(message.getBody(), TransformModel.class);
					if (logger.isDebugEnabled())
						logger.debug("Received event " + model);
					modelCache.put(model.getId(), model);
					redisService.saveData(RedisService.TRANSFORM, model.getId(), model);
				} catch (Exception e) {
					logger.error("Subscrib transform model error.", e);
				}
			}
		}, channel);
	}

	private TransformModel syncModelById(String modelId) {
		TransformModel model = modelCache.get(modelId);
		if (model != null)
			return model;
		try {
			model = (TransformModel) redisService.loadDataByTableAndKey(RedisService.TRANSFORM, modelId);
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
						A msg = grab(in);
						if (logger.isDebugEnabled())
							logger.debug("Receive data for transform : " + msg.toString());
						Map<String, Object> json = mapper.readValue(msg.toString(), Map.class);
						Object instanceId = json.get("instanceId");
						Object modelId = json.get("modelId");
						if (instanceId != null && modelId != null) {
							String mid = modelId.toString();
							TransformModel model = syncModelById(mid);//获取挖掘模型
							json.put("transformModel", model);
						}
						if (logger.isDebugEnabled())
							logger.debug("Add transform model is : " + mapper.writeValueAsString(json));
						push(out, (B) mapper.writeValueAsString(json));
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
