package org.coredata.core.model.api.dto;

import java.util.List;

/**
 * 该方法用于拼接展示相关实例化后的数据
 * @author sushi
 *
 */
public class InstanceVo {

	private Content root;

	private List<Content> subs;

	public Content getRoot() {
		return root;
	}

	public void setRoot(Content root) {
		this.root = root;
	}

	public List<Content> getSubs() {
		return subs;
	}

	public void setSubs(List<Content> subs) {
		this.subs = subs;
	}

}
