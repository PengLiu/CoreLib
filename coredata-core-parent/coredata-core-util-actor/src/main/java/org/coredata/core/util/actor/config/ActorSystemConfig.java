package org.coredata.core.util.actor.config;

import org.coredata.core.util.actor.DeadLetterActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;

@Configuration
public class ActorSystemConfig {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private SpringExtension springExtension;

	@Bean
	public ActorSystem actorSystem() {
		ActorSystem system = ActorSystem.create("actor-system", akkaConfiguration());
		ActorRef deadLettersSubscriber = system.actorOf(Props.create(DeadLetterActor.class), "global-dead-letters-subscriber");
		system.eventStream().subscribe(deadLettersSubscriber, DeadLetter.class);
		springExtension.initialize(applicationContext);
		return system;
	}

	@Bean
	public Config akkaConfiguration() {
		return ConfigFactory.load();
	}

}
