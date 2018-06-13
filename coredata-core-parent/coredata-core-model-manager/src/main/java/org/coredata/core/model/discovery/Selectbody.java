package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Selectbody implements Serializable {

	private static final long serialVersionUID = 1384397314568456158L;

	private List<Option> option = new ArrayList<>();

	public List<Option> getOption() {
		return option;
	}

	public void setOption(List<Option> option) {
		this.option = option;
	}

}
