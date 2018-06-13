package org.coredata.core.stream.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.DeadLetter;
import akka.actor.UntypedAbstractActor;
import akka.stream.Materializer;
import akka.stream.javadsl.RunnableGraph;

public class DeadMessage extends UntypedAbstractActor {

	private Logger logger = LoggerFactory.getLogger(DeadMessage.class);

	@SuppressWarnings("rawtypes")
	private RunnableGraph graph;

	private Materializer mat;

	private static final String STOP_FLAG = "Stop";

	@SuppressWarnings("rawtypes")
	public DeadMessage(RunnableGraph graph, Materializer mat) {
		this.graph = graph;
		this.mat = mat;
	}

	@Override
	public void onReceive(Object deadletter) throws Throwable {

		if (deadletter instanceof DeadLetter) {
			DeadLetter dead = (DeadLetter) deadletter;
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Received dead letter " + dead.message() + "-----" + dead.toString() + "-----" + dead.sender().path().name() + "-----" + dead.sender());
			}
			if (STOP_FLAG.equals(dead.message().toString())) {
				this.graph.run(mat);
			}
		} else {
			this.unhandled(deadletter);
		}

	}

}
