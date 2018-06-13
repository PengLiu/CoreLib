package org.coredata.core.stream.decision;

import java.util.ArrayList;
import java.util.List;

import org.coredata.core.metric.documents.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class TransformMetricFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static Logger logger = LoggerFactory.getLogger(TransformMetricFlow.class);

	public final Inlet<A> in = Inlet.create("TransformMetricFlow.in");

	public final Outlet<B> out = Outlet.create("TransformMetricFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	@Override
	public FlowShape<A, B> shape() {
		return flow;
	}

	@Override
	public GraphStageLogic createLogic(Attributes attr) throws Exception {
		return new GraphStageLogic(shape()) {
			{
				setHandler(in, new AbstractInHandler() {

					@SuppressWarnings("unchecked")
					@Override
					public void onPush() throws Exception {
						A metric = grab(in);
						if (logger.isDebugEnabled())
							logger.debug("Receive business metric is:" + metric.toString());
						List<Metric> metrics = new ArrayList<>();
						Metric m = JSON.parseObject(metric.toString(), Metric.class);
						if (m != null)
							metrics.add(m);
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
