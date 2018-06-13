package org.coredata.core.olap.model.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.coredata.core.repositories")
public class ElasticsearchConfig {

}
