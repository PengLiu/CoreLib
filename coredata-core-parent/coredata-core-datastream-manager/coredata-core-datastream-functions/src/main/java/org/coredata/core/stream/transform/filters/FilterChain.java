package org.coredata.core.stream.transform.filters;

import org.coredata.core.stream.vo.TransformData;

/**
 * 过滤器链接口
 * @author sushiping
 *
 */
public interface FilterChain {

	/**
	 * 注册过滤器方法
	 * @param filter
	 * @return
	 */
	public FilterChain registFilter(Filter filter);

	/**
	 * 过滤器链执行方法
	 */
	public void doFilter(TransformData response);

}
