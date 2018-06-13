package org.coredata.core.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJpaRepositories(value = { "org.coredata.core.repositories", "org.coredata.core.data.repositories" })
@EnableNeo4jRepositories(basePackages = "org.coredata.core.entities.repositories")
@EntityScan(basePackages = { "org.coredata.core.data.entities" })
@EnableScheduling
public class DataImportConfig {

}
