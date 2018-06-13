package org.coredata.core.olap.model.services;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

@Component("dimKeyGen")
public class CacheKeyGen implements KeyGenerator {
	public Object generate(Object target, Method method, Object... params) {
		StringBuilder sb = new StringBuilder();
		sb.append(target.getClass().getSimpleName()).append("-").append(method.getName());

		if (params != null) {
			for (Object param : params) {
				sb.append("-").append(param.getClass().getSimpleName()).append(":").append(param);
			}
		}
		return sb.toString();
	}

}
