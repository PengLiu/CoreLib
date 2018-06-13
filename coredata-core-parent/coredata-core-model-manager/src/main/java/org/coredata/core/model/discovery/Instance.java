package org.coredata.core.model.discovery;

import java.io.Serializable;
import java.util.List;

import org.coredata.core.util.encryption.PersistEncrypted;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 初始化实例对象
 * @author sushi
 *
 */
public class Instance implements Serializable {

	private static final long serialVersionUID = 1439961420456416935L;

	private String sourcemodel;

	@PersistEncrypted
	private List<Datasource> datasource;

	private String type;

	/**
	 * 对应的资源类型
	 */
	private String restype;

	private String resfullType;

	/**
	 * 实例化资源属性集合，包含需要保存和显示的属性
	 */
	private List<Property> property;

	private Instantiator instantiator;

	/**
	 * 绑定子资源的关系
	 */
	private List<Relation> relation;

	/**
	 * customerId，默认空值
	 */
	@JSONField(serialize = false, deserialize = false)
	private String customerId = "";

	/**
	 * 是否批量发现，默认否
	 */
	@JSONField(serialize = false, deserialize = false)
	private boolean batchDiscover = false;

	/**
	 * 设置扩展属性
	 */
	@JSONField(serialize = false, deserialize = false)
	private String extendProperties;

	public String getSourcemodel() {
		return sourcemodel;
	}

	public void setSourcemodel(String sourcemodel) {
		this.sourcemodel = sourcemodel;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Instantiator getInstantiator() {
		return instantiator;
	}

	public void setInstantiator(Instantiator instantiator) {
		this.instantiator = instantiator;
	}

	public List<Datasource> getDatasource() {
		return datasource;
	}

	public void setDatasource(List<Datasource> datasource) {
		this.datasource = datasource;
	}

	public String getRestype() {
		return restype;
	}

	public void setRestype(String restype) {
		this.restype = restype;
	}

	public List<Property> getProperty() {
		return property;
	}

	public void setProperty(List<Property> property) {
		this.property = property;
	}

	public List<Relation> getRelation() {
		return relation;
	}

	public void setRelation(List<Relation> relation) {
		this.relation = relation;
	}

	public String getResfullType() {
		return resfullType;
	}

	public void setResfullType(String resfullType) {
		this.resfullType = resfullType;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public boolean isBatchDiscover() {
		return batchDiscover;
	}

	public void setBatchDiscover(boolean batchDiscover) {
		this.batchDiscover = batchDiscover;
	}

	public String getExtendProperties() {
		return extendProperties;
	}

	public void setExtendProperties(String extendProperties) {
		this.extendProperties = extendProperties;
	}

}
