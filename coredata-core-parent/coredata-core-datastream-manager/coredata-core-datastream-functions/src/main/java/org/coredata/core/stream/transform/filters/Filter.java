package org.coredata.core.stream.transform.filters;

import org.coredata.core.stream.vo.TransformData;

/**
 * 清洗模型自定义过滤器
 *
 * @author sushiping
 *
 */
public interface Filter {

	/**
	 * 初始化过滤器调用的方法
	 */
	public void init();

	/**
	 * 进行过滤时调用的方法
	 */
	public void doFilter(TransformData response, FilterChain chain);

	/**
	 * 过滤器完毕，销毁方法
	 */
	public void destroy();

}
