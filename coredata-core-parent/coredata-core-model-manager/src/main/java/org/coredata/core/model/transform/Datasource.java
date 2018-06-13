package org.coredata.core.model.transform;

import java.io.Serializable;

public class Datasource implements Serializable {

	private static final long serialVersionUID = 4079314239583871243L;

	private String source;

	private Datatype datatype;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Datatype getDatatype() {
		return datatype;
	}

	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}

}
