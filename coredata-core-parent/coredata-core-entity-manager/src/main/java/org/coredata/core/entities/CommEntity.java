package org.coredata.core.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Properties;

@NodeEntity
public class CommEntity implements Serializable {

	private static final long serialVersionUID = -1969955832878535420L;

	@Id
	@GeneratedValue
	private Long id;

	@Index
	private String entityId;

	@Index
	private String token;

	private String name;

	private String type;

	@Properties(prefix = "props", allowCast = true)
	private Map<String, Object> props = new HashMap<>();

	private long createdTime = System.currentTimeMillis();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public void addProp(String key, Object val) {
		props.put(key, val);
	}

	public Object getProp(String key) {
		return props.get(key);
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	@Override
	public String toString() {
		return "CommEntity [id=" + id + ", entityId=" + entityId + ", token=" + token + ", name=" + name + ", type=" + type + ", props=" + props
				+ ", createdTime=" + createdTime + "]";
	}

}