package org.coredata.core.framework.agentmanager.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

	private static ApplicationContext ctx;

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		SpringContextUtil.ctx = ctx;
	}

	/**
	 * 该方法用于获取非Spring管理类中的bean
	 * @param clazz
	 * @return
	 */
	public static <T> T getSpringBean(Class<T> clazz) {
		return ctx.getBean(clazz);
	}

}
