package org.coredata.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApp {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

}
