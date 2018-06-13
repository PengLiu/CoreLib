package org.coredata.core.olap.model.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class OlapDimIndex {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	private String id;

	private String name;

	private String description;

	/**
	 * 来源索引
	 */
	private String srcIndex;

	/**
	 * 在Fact表中的引用Id
	 */
	private String refName;

	private String factRefId;

	private String dimRefId;

	/**
	 * 属性定义
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<OlapFieldDef> deminsionFields = new HashSet<>();

	/**
	 * 与实时表建立关联是，如果未找到则忽略该维度
	 */
	private boolean ignoreIfNotExsit = true;

	public void addFieldDef(OlapFieldDef fieldDef) {
		deminsionFields.add(fieldDef);
	}

	public boolean isIgnoreIfNotExsit() {
		return ignoreIfNotExsit;
	}

	public void setIgnoreIfNotExsit(boolean ignoreIfNotExsit) {
		this.ignoreIfNotExsit = ignoreIfNotExsit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<OlapFieldDef> getDeminsionFields() {
		return deminsionFields;
	}

	public void setDeminsionFields(Set<OlapFieldDef> deminsionFields) {
		this.deminsionFields = deminsionFields;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSrcIndex() {
		return srcIndex;
	}

	public void setSrcIndex(String srcIndex) {
		this.srcIndex = srcIndex;
	}

	public String getId() {
		return id;
	}

	public String getRefName() {
		return refName;
	}

	public void setRefName(String refName) {
		this.refName = refName;
	}

	public String getFactRefId() {
		return factRefId;
	}

	public void setFactRefId(String factRefId) {
		this.factRefId = factRefId;
	}

	public String getDimRefId() {
		return dimRefId;
	}

	public void setDimRefId(String dimRefId) {
		this.dimRefId = dimRefId;
	}

}
