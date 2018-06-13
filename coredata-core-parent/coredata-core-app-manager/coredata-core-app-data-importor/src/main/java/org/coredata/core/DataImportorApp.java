package org.coredata.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = { "org.coredata.core.data.repositories", "org.coredata.core.model.repositories" })
@EntityScan(basePackages = { "org.coredata.core.data.entities", "org.coredata.core.model.entities", "org.coredata.core.olap.model.entities" })
@EnableAsync
public class DataImportorApp {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DataImportorApp.class, args);
	}
}
