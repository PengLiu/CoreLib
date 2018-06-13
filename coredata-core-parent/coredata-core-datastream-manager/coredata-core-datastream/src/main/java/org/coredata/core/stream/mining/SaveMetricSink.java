package org.coredata.core.stream.mining;

import java.util.List;

import org.coredata.core.metric.documents.Metric;
import org.coredata.core.metric.services.MetricService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.stream.Attributes;
import akka.stream.Inlet;
import akka.stream.SinkShape;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.GraphStage;
import akka.stream.stage.GraphStageLogic;

public class SaveMetricSink<A> extends GraphStage<SinkShape<A>> {

	private static Logger logger = LoggerFactory.getLogger(SaveMetricSink.class);

	public final Inlet<A> in = Inlet.create("SaveMetricSink.in");

	private final SinkShape<A> shape = SinkShape.of(in);

	private MetricService metricService;

	public SaveMetricSink(MetricService metricService) {
		this.metricService = metricService;
	}

	@Override
	public SinkShape<A> shape() {
		return shape;
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
							metricService.save(metrics);
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
