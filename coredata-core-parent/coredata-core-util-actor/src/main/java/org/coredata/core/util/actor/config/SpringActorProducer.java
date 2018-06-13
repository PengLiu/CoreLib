package org.coredata.core.util.actor.config;

import org.springframework.context.ApplicationContext;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;

public class SpringActorProducer implements IndirectActorProducer {

	private final ApplicationContext applicationContext;

	private final String actorBeanName;

	private Object[] objects = null;
	
	public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName) {
		this.applicationContext = applicationContext;
		this.actorBeanName = actorBeanName;		
	}

	public SpringActorProducer(ApplicationContext applicationContext, String actorBeanName, Object... objects) {
		this.applicationContext = applicationContext;
		this.actorBeanName = actorBeanName;
		this.objects = objects;
	}

	@Override
	public Class<? extends Actor> actorClass() {
		return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
	}

	@Override
	public Actor produce() {
		if (objects != null) {
			return (Actor) applicationContext.getBean(actorBeanName, objects);
		}
		return (Actor) applicationContext.getBean(actorBeanName);
	}

}
