package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConditioncheckField implements Serializable {

	private static final long serialVersionUID = -5212906061684172840L;

	private String id;

	private String title;

	private String index;

	private String datatype;

	private List<Resultarea> resultarea = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public List<Resultarea> getResultarea() {
		return resultarea;
	}

	public void setResultarea(List<Resultarea> resultarea) {
		this.resultarea = resultarea;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
