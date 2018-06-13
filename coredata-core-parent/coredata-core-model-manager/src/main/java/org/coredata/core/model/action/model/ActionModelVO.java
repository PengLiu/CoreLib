package org.coredata.core.model.action.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 页面展示定义的控制模型
 *
 */
public class ActionModelVO implements Serializable {

	private static final long serialVersionUID = 6221849619014577189L;

	private String id;

	private List<ActionVO> action = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



	public List<ActionVO> getAction() {
		return action;
	}

	public void setAction(List<ActionVO> action) {
		this.action = action;
	}

}
