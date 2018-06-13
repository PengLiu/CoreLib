package org.coredata.core.model.transform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Metadata implements Serializable {

	private static final long serialVersionUID = -8390603427013409065L;

	private List<TransformField> field = new ArrayList<>();

	public List<TransformField> getField() {
		return field;
	}

	public void setField(List<TransformField> field) {
		this.field = field;
	}

}
