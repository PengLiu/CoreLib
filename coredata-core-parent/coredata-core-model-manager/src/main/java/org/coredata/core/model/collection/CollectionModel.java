package org.coredata.core.model.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 定义的数据采集模型
 * @author sushi
 *
 */
public class CollectionModel implements Serializable {

	private static final long serialVersionUID = 2458769076887134872L;

	private String id;

	private String restype;

	private String type;

	private String version;

	private String name;

	private String origin;

	private Storage storage;

	@PersistEncrypted
	private List<Collector> collector = new ArrayList<>();

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

	public String getRestype() {
		return restype;
	}

	public void setRestype(String restype) {
		this.restype = restype;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public List<Collector> getCollector() {
		return collector;
	}

	public void setCollector(List<Collector> collector) {
		this.collector = collector;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		CollectionModel other = (CollectionModel) obj;
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
