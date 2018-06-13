package org.coredata.core.model.transform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 定义的清洗模型
 * @author sushi
 *
 */
public class TransformModel implements Serializable {

	private static final long serialVersionUID = -6380899725707842005L;

	private String id;

	private String restype;

	private String type;

	private String version;

	private String name;

	/**
	 * 储存规则，到年、月、天、小时、分钟等
	 */
	private String storagetype;

	/**
	 * 设置原配置
	 */
	private String origin;

	@PersistEncrypted
	private List<Transform> transform = new ArrayList<>();

	/**
	 * 是否需要存入hdfs标识，默认为是
	 */
	private String persistence;

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

	public List<Transform> getTransform() {
		return transform;
	}

	public void setTransform(List<Transform> transform) {
		this.transform = transform;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStoragetype() {
		return storagetype;
	}

	public void setStoragetype(String storagetype) {
		this.storagetype = storagetype;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getPersistence() {
		return persistence;
	}

	public void setPersistence(String persistence) {
		this.persistence = persistence;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
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
		TransformModel other = (TransformModel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
