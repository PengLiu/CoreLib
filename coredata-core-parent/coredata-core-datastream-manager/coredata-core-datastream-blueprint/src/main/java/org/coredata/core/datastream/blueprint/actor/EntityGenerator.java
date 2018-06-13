package org.coredata.core.datastream.blueprint.actor;

import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.coredata.core.datastream.blueprint.vo.EntityFragment;
import org.coredata.core.entities.CommEntity;
import org.coredata.core.entities.services.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import akka.Done;
import akka.actor.AbstractActor;

@Component
@Scope("prototype")
public class EntityGenerator extends AbstractActor {

	private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	private ObjectMapper mapper = new ObjectMapper();

	private String token;

	private Invocable inv = null;

	@Autowired
	private EntityService entityService;

	public EntityGenerator(EntityFragment fragment, String token) {
		this.token = token;
		try {
			engine.eval(fragment.getScript());
			inv = (Invocable) engine;
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, record -> {			
			String entityInJson = (String) inv.invokeFunction("generate", record, token);
			CommEntity[] entities = mapper.readValue(entityInJson, CommEntity[].class);
			entityService.saveAsync(entities);
			getSender().tell(Done.getInstance(), getSelf());
		}).match(List.class, records -> {
			ArrayNode data = (ArrayNode) mapper.valueToTree(records);
			String entityInJson = (String) inv.invokeFunction("generate", data, token);
			CommEntity[] entities = mapper.readValue(entityInJson, CommEntity[].class);
			entityService.saveAsync(entities);
			getSender().tell(Done.getInstance(), getSelf());
		}).build();

	}

}