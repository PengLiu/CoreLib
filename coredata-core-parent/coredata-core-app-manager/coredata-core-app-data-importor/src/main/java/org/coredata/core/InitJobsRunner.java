package org.coredata.core;

import org.coredata.core.data.service.PluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitJobsRunner implements ApplicationRunner {
	
	@Autowired
	private PluginService service;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		service.initScheduleJobs();
	}

}
