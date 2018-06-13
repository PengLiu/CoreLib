package org.coredata.core.datastream.blueprint.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.coredata.core.datastream.blueprint.actor.BluePrintActor;
import org.coredata.core.datastream.blueprint.exception.BluePrintException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.kafka.javadsl.Consumer.Control;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.RunnableGraph;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

public class BluePrint {

	private Logger logger = LoggerFactory.getLogger(BluePrint.class);

	private String token;

	private List<DataWarehouseFragment> dwFragments = new ArrayList<>();

	private List<EntityFragment> entityFragments = new ArrayList<>();

	private RunnableGraph<Control> graph;

	private ActorMaterializer materializer;

	private Control control;

	private ActorRef runner;

	public void run() throws BluePrintException {

		try {
			control = graph.run(materializer);
		} catch (Throwable e) {
			throw new BluePrintException("Run graph error.", e);
		}

	}

	public void stop() {

		if (control != null) {
			control.shutdown();
		}

		if (materializer != null) {
			materializer.shutdown();
		}

		if (runner != null) {
			Timeout askTimeout = Timeout.apply(5, TimeUnit.SECONDS);
			Future<Object> future = Patterns.ask(runner, BluePrintActor.Cmd.Cleanup, askTimeout);
			try {
				Await.result(future, askTimeout.duration());
			} catch (Exception e) {
				logger.error("Stop blue print error.", e);
			}
		}

	}

	public void addDataWarehouseFragment(DataWarehouseFragment dataWarehouseFragment) {
		dwFragments.add(dataWarehouseFragment);
	}

	public void addEntityFragment(EntityFragment entityFragment) {
		entityFragments.add(entityFragment);
	}

	public int getFlows() {
		int flows = 0;
		flows += entityFragments.size();
		flows += dwFragments.size();
		return flows;
	}

	public RunnableGraph<Control> getGraph() {
		return graph;
	}

	public void setGraph(RunnableGraph<Control> graph) {
		this.graph = graph;
	}

	public ActorMaterializer getMaterializer() {
		return materializer;
	}

	public void setMaterializer(ActorMaterializer materializer) {
		this.materializer = materializer;
	}

	public List<EntityFragment> getEntityFragments() {
		return entityFragments;
	}

	public void setRunner(ActorRef runner) {
		this.runner = runner;
	}

	public ActorRef getRunner() {
		return runner;
	}

	public List<DataWarehouseFragment> getDwFragments() {
		return dwFragments;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
