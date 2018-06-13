package org.coredata.core.metric.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@Configuration
@EnableScheduling
@EnableElasticsearchRepositories(basePackages = "org.coredata.core.metric.repositories")
@EntityScan(basePackages = "org.coredata.core.metric.documents")
public class MetricRespConfig {

}
