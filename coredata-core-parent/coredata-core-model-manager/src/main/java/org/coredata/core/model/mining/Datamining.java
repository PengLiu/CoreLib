package org.coredata.core.model.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 定义的数据挖掘模型
 * @author sushiping
 *
 */
public class Datamining implements Serializable {

	private static final long serialVersionUID = 159293685913583648L;

	private String id;

	@PersistEncrypted
	private String name;

	private String category;

	private String datatype;

	@PersistEncrypted
	private List<Datasource> datasource = new ArrayList<>();

	@PersistEncrypted
	private Type type;

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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public List<Datasource> getDatasource() {
		return datasource;
	}

	public void setDatasource(List<Datasource> datasource) {
		this.datasource = datasource;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Datamining other = (Datamining) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
