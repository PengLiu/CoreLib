package org.coredata.core.framework.agentmanager.monitor;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AgentMonitor {

	private static ConcurrentHashMap<String, Integer> agentsTasks = new ConcurrentHashMap<>();

	public static void addCount(String agentId, int taskCount) {
		agentsTasks.put(agentId, taskCount);
	}

	public static int getTaskCount() {
		int size = 0;
		for (Entry<String, Integer> entry : agentsTasks.entrySet()) {
			size += entry.getValue();
		}
		return size;
	}
	
	

}
