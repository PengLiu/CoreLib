package org.coredata.core.model.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 数据挖掘模型对象
 * @author sushiping
 *
 */
public class DataminingModel implements Serializable {

	private static final long serialVersionUID = -1611616341600197450L;

	private String id;

	private String version;

	private String type;

	private String name;

	@PersistEncrypted
	private List<Datamining> mining = new ArrayList<>();

	private String origin;

	/**
	 * 是否系统默认模型，默认是
	 */
	private int isSystem = 1;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Datamining> getMining() {
		return mining;
	}

	public void setMining(List<Datamining> mining) {
		this.mining = mining;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		DataminingModel other = (DataminingModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
