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
public class OlapFactIndex {

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	private String id;

	private String name;

	private String description;

	//data importing job id
	private String jobId;

	/**
	 * 整合后数据存放的索引
	 */
	private String indexName;

	/**
	 * 来源索引
	 */
	private String srcIndex;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<OlapFieldDef> factFields = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<OlapDimIndex> dimensions = new HashSet<>();

	public void addDimension(OlapDimIndex dimension) {
		dimensions.add(dimension);
	}

	public void addFieldDef(OlapFieldDef fieldDef) {
		factFields.add(fieldDef);
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

	public Set<OlapFieldDef> getFactFields() {
		return factFields;
	}

	public void setFactFields(Set<OlapFieldDef> factFields) {
		this.factFields = factFields;
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

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Set<OlapDimIndex> getDimensions() {
		return dimensions;
	}

	public void setDimensions(Set<OlapDimIndex> dimensions) {
		this.dimensions = dimensions;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

}