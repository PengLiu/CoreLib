package org.coredata.core.agent.collector.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.coredata.core.util.common.MethodUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;

@Component
@Scope("prototype")
public class TaskManager extends UntypedAbstractActor {

	private Set<String> taskIds = new HashSet<>();

	public static final String TASKS = "task_list";

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object obj) throws Throwable {
		if (obj instanceof Task) {
			Task task = (Task) obj;
			switch (task.getTaskType()) {
			case RunOnce:
				//TODO: Add actor load balancer
				ActorRef actor = getContext().actorOf(Props.create(TaskOnceRunner.class));
				actor.tell(task.getCmds(), getSelf());
				break;
			case RunPeriod:
				Optional<ActorRef> child = getContext().findChild(task.getTaskId());
				if (!child.isPresent())
					actor = getContext().actorOf(Props.create(TaskPeriodRunner.class, task), task.getTaskId());
				taskIds.add(task.getTaskId());
				break;
			case RunStep:
				actor = getContext().actorOf(Props.create(TaskStepRunner.class));
				actor.tell(task.getCmds(), getSelf());
				break;
			case RunReceiving:
				Optional<ActorRef> rchild = getContext().findChild(MethodUtil.md5(task.getTaskId() + TaskType.RunReceiving.toString()));
				if (!rchild.isPresent())
					actor = getContext().actorOf(Props.create(TaskReceivingRunner.class), MethodUtil.md5(task.getTaskId() + TaskType.RunReceiving.toString()));
				break;
			case ReceivingData:
				String taskId = task.getTaskId();
				Optional<ActorRef> receivingActor = getContext().findChild(MethodUtil.md5(taskId + TaskType.RunReceiving.toString()));
				if (receivingActor.isPresent())
					receivingActor.get().tell(task.getNoPeriodCmds(), ActorRef.noSender());
			}
		} else if (obj instanceof String) {
			// 取得任务列表
			if (TASKS.equals(obj)) {
				getSender().tell(taskIds.toArray(new String[] {}), ActorRef.noSender());
			}
		} else if (obj instanceof Collection) {
			((Collection<String>) obj).forEach(taskId -> {
				Optional<ActorRef> actor = getContext().findChild(taskId);
				if (actor.isPresent()) {
					getContext().stop(actor.get());
				}
				Optional<ActorRef> receivingActor = getContext().findChild(MethodUtil.md5(taskId + TaskType.RunReceiving.toString()));
				if (receivingActor.isPresent()) {
					getContext().stop(receivingActor.get());
				}
				taskIds.remove(taskId);
			});
		} else if (obj instanceof Long) {
			for (ActorRef actor : getContext().getChildren()) {
				actor.tell(obj, getSelf());
			}
		} else {
			unhandled(obj);
		}
	}
}