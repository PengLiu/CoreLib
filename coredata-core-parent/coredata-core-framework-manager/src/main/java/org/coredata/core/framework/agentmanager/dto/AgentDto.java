package org.coredata.core.framework.agentmanager.dto;

/**
 * 该类用于封装Agent列表参数及分页信息
 * @author sushi
 *
 */
public class AgentDto {

	private int pageNum;

	private int pageSize;

	/**
	 * 用于保存或搜索条件
	 */
	private String remarks;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
