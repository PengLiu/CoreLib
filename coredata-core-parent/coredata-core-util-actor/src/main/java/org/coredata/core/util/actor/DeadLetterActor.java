package org.coredata.core.util.actor;

import akka.actor.AbstractActor;
import akka.actor.DeadLetter;

public class DeadLetterActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(DeadLetter.class, deadletter -> {
			System.err.println("Received dead letter :" + deadletter.message());
		}).build();
	}

}
