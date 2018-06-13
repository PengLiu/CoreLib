package org.coredata.core.stream.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.model.mining.Expression;
import org.coredata.core.stream.mining.entity.MetricInfo;
import org.coredata.core.stream.mining.entity.MiningData;
import org.coredata.core.stream.mining.functions.MiningFunctions;
import org.coredata.core.stream.service.StreamService;
import org.coredata.core.stream.util.ModelExpHelper;
import org.coredata.core.stream.vo.TransformData;
import org.coredata.core.util.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class MiningDataFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static Logger logger = LoggerFactory.getLogger(MiningDataFlow.class);

	private final String METRIC_INFO = "metricInfo";

	public final Inlet<A> in = Inlet.create("MiningDataFlow.in");

	public final Outlet<B> out = Outlet.create("MiningDataFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private RedisService redisService;

	private StreamService miningService;

	public MiningDataFlow(RedisService redisService, StreamService miningService) {
		this.redisService = redisService;
		this.miningService = miningService;
	}

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	@Override
	public GraphStageLogic createLogic(Attributes inheritedAttributes) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {
					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						List<MiningData> datas = (List<MiningData>) grab(in);
						List<Metric> metrics = new ArrayList<>();
						datas.forEach(data -> {
							try {
								MiningData md = data;
								MetricInfo info = md.getInfo();
								TransformData resp = md.getRes();
								String aid = md.getAid();
								Expression exp = md.getExp();
								String miningId = md.getMiningId();
								if (info.needBinding(aid, resp.getInstanceId()))
									MiningFunctions.binding(info, resp, aid);
								info.setTasktime(resp.getTasktime());
								info.getParams().addAll(exp.getParam());
								Map<String, String> alias = info.getAliasToCmdName();
								alias.put(aid, resp.getName());
								if (info.cacheData(aid, resp)) {
									long startTime = System.currentTimeMillis();
									Object obj = info.mining();
									long endTime = System.currentTimeMillis();
									if (logger.isDebugEnabled() && endTime - startTime > 150) {
										logger.debug("Mining time is:" + (endTime - startTime));
										logger.debug("Metric is:" + info.getId());
									}
									miningService.setScale(info);
									Metric metric = ModelExpHelper.mining(info, resp, obj);
									if (metric != null) {
										metrics.add(metric);
									}
									redisService.deleteDataByTableAndKey(METRIC_INFO, resp.getInstanceId() + resp.getModelid() + miningId + exp.getMetric());
								} else {
									redisService.saveData(METRIC_INFO, resp.getInstanceId() + resp.getModelid() + miningId + exp.getMetric(), info);
								}
							} catch (Exception e) {
								logger.error("Mining Data Error.", e);
							}
						});
						//push TSDBMetric
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
