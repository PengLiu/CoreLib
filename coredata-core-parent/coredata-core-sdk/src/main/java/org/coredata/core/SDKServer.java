package org.coredata.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = { "org.coredata.core.entities.repositories" })
@EntityScan(basePackages = { "org.coredata.core.data.entities", "org.coredata.core.olap.model.entities" })
public class SDKServer {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(SDKServer.class).build().run(args);
	}

}
