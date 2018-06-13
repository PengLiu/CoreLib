package org.coredata.core.agent.collector.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coredata.core.agent.collector.Cmd;

public class Task {

	private String taskId;

	private TaskType taskType = TaskType.RunPeriod;

	private Map<Long, List<Cmd>> cmds = new HashMap<>();

	private List<Cmd> noPeriodCmds = new ArrayList<>();

	public Task(String taskId, TaskType taskType) {
		this.taskId = taskId;
		this.taskType = taskType;
	}

	public Task(String taskId) {
		this(taskId, TaskType.RunPeriod);
	}

	public void addCmd(Cmd cmd) {
		if (cmds.containsKey(cmd.getPeriod())) {
			cmds.get(cmd.getPeriod()).add(cmd);
		} else {
			List<Cmd> tmp = new ArrayList<>();
			tmp.add(cmd);
			cmds.put(cmd.getPeriod(), tmp);
		}
	}

	public void addNoPeriodCmd(Cmd cmd) {
		noPeriodCmds.add(cmd);
	}

	public String getTaskId() {
		return taskId;
	}

	@Override
	public String toString() {
		return "Task [taskId=" + taskId + ", cmds=" + cmds + "]";
	}

	public Map<Long, List<Cmd>> getCmds() {
		return cmds;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public List<Cmd> getNoPeriodCmds() {
		return noPeriodCmds;
	}

}