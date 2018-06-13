package org.coredata.core.business.documents;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "packetanalys_#{@indexUtils.daySuffix()}", type = "packetanalys", shards = 5, replicas = 1, refreshInterval = "5s")
public class PacketAnalys {

	@Id
	private String id;

	@Field(type = FieldType.keyword)
	private String token;

	@Field(type = FieldType.text)
	private String content;

	@Field(type = FieldType.Date)
	private long taskTime = System.currentTimeMillis();
	
	@Field(type = FieldType.keyword)
	private String ip;
	
	@Field(type = FieldType.keyword)
	private String port;
	
	@Field(type = FieldType.keyword)
	private String client_ip;

	@Field(type = FieldType.keyword)
	private String status;
	
	@Field(type = FieldType.Long)
	private long responsetime;
	
	@Field(type = FieldType.keyword)
	private String path;
	
	@Field(type = FieldType.Long)
	private long bytes_in;
	
	@Field(type = FieldType.Long)
	private long bytes_out;
	
	
	@Field(type = FieldType.Nested)
	private Map<String, Object> props;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	

	public static boolean isAttribute(String attName) {
		switch (attName) {
		case "token":
		case "taskTime":
		case "ip":
		case "port":
		case "client_ip":
		case "status":
		case "responsetime":
		case "path":
		case "bytes_in":
		case "bytes_out":
		case "content":
			return true;
		default:
			return false;
		}
	}

}
