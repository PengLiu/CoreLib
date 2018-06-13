package org.coredata.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "org.coredata.core.entities","org.coredata.core.olap.model.entities" })
@EnableNeo4jRepositories(basePackages = "org.coredata.core.entities.repositories")
public class TestApp {
	
	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}
	
}
