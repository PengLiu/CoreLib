package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Discovery implements Serializable {

	private static final long serialVersionUID = 4989386290655721337L;

	private String protocol;

	private String legend;

	private List<Field> field = new ArrayList<>();

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public List<Field> getField() {
		return field;
	}

	public void setField(List<Field> field) {
		this.field = field;
	}

	public String getLegend() {
		return legend;
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

}
