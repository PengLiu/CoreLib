package org.coredata.core.framework.agentmanager.page;

import java.util.List;

/**
 * 封装page分页对象
 * @author sushi
 *
 */
public class PageParam<T> {

	/**
	 * 当前页数，默认为1
	 */
	private int pageNo = 1;

	/**
	 * 每页显示条数
	 */
	private int limit;

	/**
	 * 记录总数
	 */
	private int totalCount;

	/**
	 * 总页数
	 */
	private int totalPage;

	/**
	 * 记录起始行数
	 */
	private int startIndex;

	/**
	 * 记录结束行数
	0	 */
	private int lastIndex;

	/**
	 * 存放结果集
	 */
	private List<T> resultList;

	/**
	 * 构造方法
	 * @param pageNo
	 * @param limit
	 * @param list
	 */
	public PageParam(int pageNo, int limit, int totalCount, List<T> list) {
		//设置每页显示记录数
		setLimit(limit);
		//设置当前页数
		setPageNo(pageNo);
		//设置总记录数
		setTotalCount(totalCount);
		//设置总页数
		setTotalPage();
		//设置起始记录行数
		setStartIndex();
		//设置结束记录行数
		setLastIndex();
		//设置结果集
		setResultList(list);
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit == 0 ? 1 : limit;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage() {
		if (totalCount % limit == 0) {
			this.totalPage = totalCount / limit;
		} else {
			this.totalPage = (totalCount / limit) + 1;
		}
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex() {
		this.startIndex = (pageNo - 1) * limit;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	public void setLastIndex() {
		if (totalCount < limit) {
			this.lastIndex = totalCount;
		} else if ((totalCount % limit == 0) || (totalCount % limit != 0 && pageNo < totalPage)) {
			this.lastIndex = pageNo * limit;
		} else if (totalCount % limit != 0 && pageNo == totalPage) {//最后一页
			this.lastIndex = totalCount;
		}
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

}
