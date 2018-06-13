package org.coredata.core.model.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
@EnableJpaRepositories(basePackages = "org.coredata.core.model.repositories")
@EntityScan(basePackages = "org.coredata.core.model.entities")
public class ModelEntityConfig {

}
