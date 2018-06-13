package org.coredata.core.model.decision;

import java.io.Serializable;
import java.util.List;

public class Decision implements Serializable {

	private static final long serialVersionUID = -4490133553338004882L;

	private String id;

	private String name;

	private Associatedres associatedres;

	private List<DecisionRule> rule;

	private Boolean enable;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DecisionRule> getRule() {
		return rule;
	}

	public void setRule(List<DecisionRule> rule) {
		this.rule = rule;
	}

	public Associatedres getAssociatedres() {
		return associatedres;
	}

	public void setAssociatedres(Associatedres associatedres) {
		this.associatedres = associatedres;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

}
