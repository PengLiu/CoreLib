package org.coredata.core.data.readers.crawler;

import java.util.List;

public class SpiderParam {
	private SpiderLogin login;
	private List<SpiderTask> tasks;

	public SpiderLogin getLogin() {
		return login;
	}

	public void setLogin(SpiderLogin login) {
		this.login = login;
	}

	public List<SpiderTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<SpiderTask> tasks) {
		this.tasks = tasks;
	}

}
