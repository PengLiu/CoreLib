package org.coredata.core.datastream.blueprint.vo;

import org.coredata.core.olap.model.entities.OlapModel;

public class DataWarehouseFragment {

	private OlapModel model;

	public DataWarehouseFragment() {

	}

	public DataWarehouseFragment(OlapModel model) {
		this.model = model;
	}

	public OlapModel getModel() {
		return model;
	}

	public void setModel(OlapModel model) {
		this.model = model;
	}

}
