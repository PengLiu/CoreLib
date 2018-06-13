package org.coredata.core.model.mining;

import java.io.Serializable;
import java.util.List;

/**
 * 数据挖掘模型中的metadata信息
 * @author 超
 *
 */
public class Metadata implements Serializable {

	private static final long serialVersionUID = -2750171658970376971L;

	private List<Field> field;

	public List<Field> getField() {
		return field;
	}

	public void setField(List<Field> field) {
		this.field = field;
	}

}
