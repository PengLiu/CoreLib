package org.coredata.core.stream.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.coredata.core.model.mining.Datamining;

/**
 * 数据挖掘前准备工作对象
 * @author sushi
 *
 */
public class PrepareMiningData implements Serializable {

	private static final long serialVersionUID = -4503670080481438676L;

	private Map<String, String> alias = new HashMap<>();

	private Map<String, String> instAlias = new HashMap<>();

	boolean needInstance = false;

	/**
	 * 用于保存不支持的命令
	 */
	private Set<String> notSupportAlias = new HashSet<>();

	private Datamining datamining;

	public Map<String, String> getAlias() {
		return alias;
	}

	public void setAlias(Map<String, String> alias) {
		this.alias = alias;
	}

	public Map<String, String> getInstAlias() {
		return instAlias;
	}

	public void setInstAlias(Map<String, String> instAlias) {
		this.instAlias = instAlias;
	}

	public boolean getNeedInstance() {
		return needInstance;
	}

	public void setNeedInstance(boolean needInstance) {
		this.needInstance = needInstance;
	}

	public Set<String> getNotSupportAlias() {
		return notSupportAlias;
	}

	public void setNotSupportAlias(Set<String> notSupportAlias) {
		this.notSupportAlias = notSupportAlias;
	}

	public Datamining getDatamining() {
		return datamining;
	}

	public void setDatamining(Datamining datamining) {
		this.datamining = datamining;
	}

}
