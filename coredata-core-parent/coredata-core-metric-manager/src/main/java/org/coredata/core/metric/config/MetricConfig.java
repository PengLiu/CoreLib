package org.coredata.core.metric.config;

import org.coredata.core.IndexUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@Configuration
@EnableScheduling
public class MetricConfig {

	@Bean
	public IndexUtils indexUtils() {
		return new IndexUtils();
	}

}
