package org.coredata.core.stream.mining;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.coredata.core.entities.ResEntity;
import org.coredata.core.model.mining.DataminingModel;
import org.coredata.core.stream.vo.TransformData;
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

public class SysMiningModelFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static Logger logger = LoggerFactory.getLogger(SysMiningModelFlow.class);

	public final Inlet<A> in = Inlet.create("SysMiningModelFlow.in");

	public final Outlet<B> out = Outlet.create("SysMiningModelFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private RedisService redisService = null;

	private ObjectMapper mapper = new ObjectMapper();

	private ConcurrentMap<String, DataminingModel> modelCache = new ConcurrentHashMap<String, DataminingModel>();

	public SysMiningModelFlow(RedisService redisService, String topic) {
		this.redisService = redisService;
		//注册挖掘模型订阅消息
		ChannelTopic channel = new ChannelTopic(topic);
		redisService.registSubscriber(new MessageListener() {
			@Override
			public void onMessage(Message message, byte[] pattern) {
				try {
					DataminingModel model = mapper.readValue(message.getBody(), DataminingModel.class);
					if (logger.isDebugEnabled())
						logger.debug("Received event " + model);
					modelCache.put(model.getId(), model);
					redisService.saveData(RedisService.MINING, model.getId(), model);
				} catch (Exception e) {
					logger.error("Subscrib mining model error.", e);
				}
			}
		}, channel);
	}

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	private String findMiningModel(String instId) {
		//TODO
		ResEntity inst = (ResEntity) redisService.loadDataByTableAndKey(RedisService.INSTANCE, instId);
		if (inst != null) {
			Map<String, Object> props = inst.getProps();
			Object model = props.get("dataminingId");
			return model == null ? null : model.toString();
		}
		return null;

	}

	private DataminingModel syncModelById(String modelId) {
		DataminingModel model = modelCache.get(modelId);
		if (model != null)
			return model;
		try {
			model = (DataminingModel) redisService.loadDataByTableAndKey(RedisService.MINING, modelId);
			modelCache.put(modelId, model);
		} catch (Exception e) {
			logger.error("Load model " + modelId + " error", e);
		}
		return model;
	}

	@Override
	public GraphStageLogic createLogic(Attributes att) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {

					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						A grab = grab(in);
						TransformData transformData = mapper.readValue(grab.toString(), TransformData.class);
						String instanceId = transformData.getInstanceId();
						String mid = findMiningModel(instanceId);
						if (mid != null) {
							DataminingModel model = syncModelById(mid);//获取挖掘模型
							transformData.setDataminingModel(model);
						}
						push(out, (B) mapper.writeValueAsString(transformData));
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
