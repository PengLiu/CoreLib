package org.coredata.core.datastream.blueprint.actor;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.Done;
import akka.actor.AbstractActor;

@Component
@Scope("prototype")
public class MetricGenerator extends AbstractActor {

	@Override
	public Receive createReceive() {

		return receiveBuilder().match(String.class, record -> {
			System.err.println("------xxxxxx--------");
			getSender().tell(Done.getInstance(), getSelf());
		}).match(List.class, records -> {
			String name = getSelf().path().toString();
			System.err.println(name + " Metric: " + records.size());
			getSender().tell(Done.getInstance(), getSelf());
		}).build();

	}

}