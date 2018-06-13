package org.coredata.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@EnableNeo4jRepositories(basePackages = "org.coredata.core.entities.repositories")
@EntityScan(basePackages = { "org.coredata.core.entities" })
@SpringBootApplication
public class TestApp {

}
