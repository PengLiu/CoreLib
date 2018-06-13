package org.coredata.core.agent.collector;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.coredata.core.agent.collector.protocol.Protocol;
import org.coredata.core.agent.collector.service.CollectService;
import org.coredata.core.agent.collector.task.Task;
import org.coredata.core.agent.collector.task.TaskManager;
import org.coredata.core.util.actor.config.SpringExtension;
import org.coredata.core.util.common.CloneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter.ConsistentHashMapper;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

@Service
public class Collector {

	@Autowired
	private ActorSystem system;

	@Autowired
	private SpringExtension springExtension;

	private ActorRef cmdRunnerRouter = null;

	private ActorRef taskManager = null;

	private ActorRef availTaskManager = null;//单独创建可用性指标的父actor，加快采集速度

	@Autowired
	private CollectService service;

	@PostConstruct
	public void initCollector() {
		//初始化协议
		Protocol.Init();

		final ConsistentHashMapper hashMapper = new ConsistentHashMapper() {
			@Override
			public Object hashKey(Object message) {
				if (message instanceof Cmd) {
					Cmd cmd = (Cmd) message;
					return cmd.getRootId() == null ? "TempId" : cmd.getRootId();
				} else {
					return null;
				}
			}
		};

		cmdRunnerRouter = system.actorOf(
				new ConsistentHashingPool(100).withHashMapper(hashMapper).props(springExtension.props("cmdRunner").withDispatcher("cmd-dispatcher")),
				"CmdRunner");

		taskManager = system.actorOf(springExtension.props("taskManager"), "TaskManager");
		availTaskManager = system.actorOf(springExtension.props("availTaskManager"), "AvailTaskManager");

		system.scheduler().schedule(Duration.Zero(), Duration.create(1, TimeUnit.SECONDS), new Runnable() {
			@Override
			public void run() {
				taskManager.tell(System.currentTimeMillis(), ActorRef.noSender());
				availTaskManager.tell(System.currentTimeMillis(), ActorRef.noSender());
			}
		}, system.dispatcher());

		ActorRef deadLettersSubscriber = system.actorOf(springExtension.props("deadMessage"), "dead-letters-subscriber");
		system.eventStream().subscribe(deadLettersSubscriber, DeadLetter.class);

	}

	public String[] getTasks() throws Exception {
		Timeout time = new Timeout(5, TimeUnit.SECONDS);
		Future<Object> future = Patterns.ask(taskManager, TaskManager.TASKS, time);
		String[] tasks = (String[]) Await.result(future, time.duration());
		return tasks;
	}

	public void delete(Collection<String> tasks) {
		taskManager.tell(tasks, ActorRef.noSender());
		availTaskManager.tell(tasks, ActorRef.noSender());
	}

	public void run(Task task) {
		taskManager.tell(task, ActorRef.noSender());
	}

	public void runAvailTask(Task task) {
		availTaskManager.tell(task, ActorRef.noSender());
	}

	public Future<Object> runNow(Cmd cmd) {
		int retry = cmd.getRetry();
		long timeout = cmd.getTimeout();
		long wait = timeout * (retry > 0 ? retry : 1);
		Timeout time = new Timeout(wait, TimeUnit.MILLISECONDS);
		Cmd tmp = CloneUtil.createCloneObj(cmd);
		tmp.appendTaskTime(System.currentTimeMillis());
		ActorRef worker = system.actorOf(Props.create(CmdRunner.class, service));
		return Patterns.ask(worker, tmp, time);
	}

	public void run(Cmd cmd, ActorRef sender) {
		Cmd tmp = CloneUtil.createCloneObj(cmd);
		tmp.appendTaskTime(System.currentTimeMillis());
		cmdRunnerRouter.tell(tmp, sender);
	}
}