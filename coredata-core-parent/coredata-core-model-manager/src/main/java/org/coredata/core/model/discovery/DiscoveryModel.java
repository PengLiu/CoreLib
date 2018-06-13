package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.coredata.core.util.encryption.PersistEncrypted;

/**
 * 发现模型对象
 * @author sushi
 *
 */
public class DiscoveryModel implements Serializable {

	private static final long serialVersionUID = -8984990845797216031L;

	private String id;

	private String type;

	private String restype;

	private String resfullType;

	private String version;

	private String origin;

	private String name;

	private List<Discovery> discovery = new ArrayList<>();

	@PersistEncrypted
	private List<Instance> instance = new ArrayList<>();

	private List<Conditioncheck> conditioncheck = new ArrayList<>();

	private List<Map<String, String>> firms = new ArrayList<>();

	/**
	 * 是否系统默认模型，默认是
	 */
	private int isSystem = 1;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRestype() {
		return restype;
	}

	public void setRestype(String restype) {
		this.restype = restype;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Instance> getInstance() {
		return instance;
	}

	public void setInstance(List<Instance> instance) {
		this.instance = instance;
	}

	public List<Discovery> getDiscovery() {
		return discovery;
	}

	public void setDiscovery(List<Discovery> discovery) {
		this.discovery = discovery;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Conditioncheck> getConditioncheck() {
		return conditioncheck;
	}

	public void setConditioncheck(List<Conditioncheck> conditioncheck) {
		this.conditioncheck = conditioncheck;
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

	/**
	 * @return the resfullType
	 */
	public String getResfullType() {
		return resfullType;
	}

	/**
	 * @param resfullType the resfullType to set
	 */
	public void setResfullType(String resfullType) {
		this.resfullType = resfullType;
	}

	@Override
	public boolean equals(Object another) {
		if (another instanceof DiscoveryModel) {
			if (id == null)
				return false;
			else
				return id.equals(((DiscoveryModel) another).getId());
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return id == null ? -1 : id.hashCode();
	}

	public List<Map<String, String>> getFirms() {
		return firms;
	}

	public void setFirms(List<Map<String, String>> firms) {
		this.firms = firms;
	}

	public int getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(int isSystem) {
		this.isSystem = isSystem;
	}

}
