package org.coredata.core.agent.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.DeadLetter;
import akka.actor.UntypedAbstractActor;

@Component
@Scope("prototype")
public class DeadMessage extends UntypedAbstractActor {

	private Logger logger = LoggerFactory.getLogger(DeadMessage.class);

	@Override
	public void onReceive(Object arg0) throws Throwable {

		if (arg0 instanceof DeadLetter) {
			DeadLetter dead = (DeadLetter) arg0;
			if (logger.isDebugEnabled()) {
				logger.debug("Received dead letter " + dead.message());
			}
		} else {
			this.unhandled(arg0);
		}

	}

}
