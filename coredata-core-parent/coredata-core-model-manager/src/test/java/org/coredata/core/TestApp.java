package org.coredata.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = {
		"org.coredata.core.entities.repositories",
		"org.coredata.core.model.repositories"})
@EntityScan(basePackages = {"org.coredata.core.entities",
		"org.coredata.core.model.entities"})
public class TestApp {

}
