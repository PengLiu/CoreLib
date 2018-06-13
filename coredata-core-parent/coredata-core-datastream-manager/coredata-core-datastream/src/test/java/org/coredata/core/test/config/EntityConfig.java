package org.coredata.core.test.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
@EnableNeo4jRepositories(basePackages = "org.coredata.core.entities.repositories")
@EntityScan(basePackages = { "org.coredata.core.entities" })
public class EntityConfig {

}
