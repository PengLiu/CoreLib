package org.coredata.core.datastream.blueprint.service;

import java.util.concurrent.TimeUnit;

import org.coredata.core.datastream.blueprint.actor.BluePrintActor;
import org.coredata.core.datastream.blueprint.exception.BluePrintException;
import org.coredata.core.datastream.blueprint.vo.BluePrint;
import org.coredata.core.util.actor.config.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

@Service
public class BluePrintService {

	@Autowired
	private ActorSystem actorSystem;

	@Autowired
	private SpringExtension springExtension;

	public void buildGraph(BluePrint bluePrint) throws BluePrintException {
		try {
			Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
			ActorRef runner = actorSystem.actorOf(springExtension.props("bluePrintActor", bluePrint));
			bluePrint.setRunner(runner);
			Future<Object> future = Patterns.ask(runner, BluePrintActor.Cmd.Init, askTimeout);
			Await.result(future, askTimeout.duration());
		} catch (Exception e) {
			throw new BluePrintException("Create blue print error.", e);
		}

	}

}