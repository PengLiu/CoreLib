package org.coredata.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "org.coredata.core.util", "org.coredata.core.metric", "org.coredata.core.alarm", "org.coredata.core.test.config",
		"org.coredata.core.stream.service", "org.coredata.core.stream.util" }) //"org.coredata.core.test.config", "org.coredata.core.entities" })
public class TestApp {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

}
