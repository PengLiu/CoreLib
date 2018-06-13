package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Field implements Serializable {

	private static final long serialVersionUID = -5090629170361221523L;

	private String id;

	private String index;

	private String type;

	private String datatype;

	private List<Validate> validate = new ArrayList<>();

	private String name;

	private String tips;

	private String defaultvalue = "";

	private Selectbody selectbody;

	private String visiblerule;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public Selectbody getSelectbody() {
		return selectbody;
	}

	public void setSelectbody(Selectbody selectbody) {
		this.selectbody = selectbody;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public List<Validate> getValidate() {
		return validate;
	}

	public void setValidate(List<Validate> validate) {
		this.validate = validate;
	}

	public String getDefaultvalue() {
		return defaultvalue;
	}

	public void setDefaultvalue(String defaultvalue) {
		this.defaultvalue = defaultvalue;
	}

	public String getVisiblerule() {
		return visiblerule;
	}

	public void setVisiblerule(String visiblerule) {
		this.visiblerule = visiblerule;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
