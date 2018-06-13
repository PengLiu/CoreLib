package org.coredata.core.alarm.config;

import org.coredata.core.IndexUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class AlarmConfig {

	@Bean
	public IndexUtils indexUtils() {
		return new IndexUtils();
	}

}
