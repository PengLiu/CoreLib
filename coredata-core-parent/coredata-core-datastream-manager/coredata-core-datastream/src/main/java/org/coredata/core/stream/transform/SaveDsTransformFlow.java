package org.coredata.core.stream.transform;

import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
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

public class SaveDsTransformFlow<A, B> extends GraphStage<FlowShape<A, B>> {

	private static final Logger logger = LoggerFactory.getLogger(SaveDsTransformFlow.class);

	public final Inlet<A> in = Inlet.create("SaveDsTransformFlow.in");

	public final Outlet<B> out = Outlet.create("SaveDsTransformFlow.out");

	private final FlowShape<A, B> flow = FlowShape.of(in, out);

	private String topic;

	public SaveDsTransformFlow(String topic) {
		this.topic = topic;
	}

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
						A msg = grab(in);
						if (logger.isDebugEnabled())
							logger.debug("Receive transform data : " + msg.toString());
						ProducerRecord<String, String> record = new ProducerRecord<>(topic, UUID.randomUUID().toString(), msg.toString());
						push(out, (B) record);
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
