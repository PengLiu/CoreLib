package org.coredata.core.alarm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
@EnableElasticsearchRepositories(basePackages = "org.coredata.core.alarm.repositories")
public class AlarmRespConfig {

}
