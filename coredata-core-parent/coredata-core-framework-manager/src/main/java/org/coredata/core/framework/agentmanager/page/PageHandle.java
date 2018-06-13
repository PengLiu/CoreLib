package org.coredata.core.framework.agentmanager.page;

/**
 * 分页助手类，包含处理分页方法
 * @author sushi
 *
 */
public interface PageHandle {

	/**
	 * 该方法用于处理相关分页语句
	 * @param sql
	 * @param pageNo
	 * @param limit
	 * @return
	 */
	public String handlePagingSQL(String sql, int pageNo, int limit);

	/**
	 * 该方法用于拼接总记录数
	 * @param sql
	 * @return
	 */
	public String handleTotalCountSQL(String sql);

}
