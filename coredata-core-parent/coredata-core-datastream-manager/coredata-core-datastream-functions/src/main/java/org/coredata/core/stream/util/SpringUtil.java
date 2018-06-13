package org.coredata.core.stream.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringUtil implements ApplicationContextAware {

	private static ApplicationContext ctx;

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		SpringUtil.ctx = ctx;
	}

	public static <T> T getBean(Class<T> clazz) {
		return ctx.getBean(clazz);
	}

}