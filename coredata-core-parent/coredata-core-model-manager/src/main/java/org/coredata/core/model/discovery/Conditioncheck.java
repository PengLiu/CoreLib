package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Conditioncheck implements Serializable {

	private static final long serialVersionUID = 1202079530730512887L;

	private String type;

	private String legend;

	private List<ConditioncheckField> field = new ArrayList<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public List<ConditioncheckField> getField() {
		return field;
	}

	public void setField(List<ConditioncheckField> field) {
		this.field = field;
	}

}
