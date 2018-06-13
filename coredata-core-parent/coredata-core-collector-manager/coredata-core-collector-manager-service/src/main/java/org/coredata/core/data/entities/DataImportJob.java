package org.coredata.core.data.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.coredata.core.data.schedule.Status;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class DataImportJob {
	/**
	 * 任务id
	 */
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String jobId;

	/**
	 * 任务名称
	 */
	private String name;

	/**
	 * 任务名称
	 */
	private String indexName;

	/**
	 * 任务描述
	 */
	@Column(length = 500)
	private String description;

	/**
	 * 任务创建账号
	 */
	private String creator;

	/**
	 * 任务最后执行时间
	 */
	private long lastRunTime;

	/**
	 * 任务更新时间
	 */
	private long createdTime = System.currentTimeMillis();

	/**
	 * 任务数据源类型
	 */
	private DataSourceType dataSourceType;

	/**
	 * 任务数据源类型
	 */
	private DataSourceType dataTargetType = DataSourceType.esWriter;

	/**
	 * 任务是否激活
	 */
	private boolean activate = true;

	/**
	 * 配置是否完整
	 */
	private String configFlag;

	private Status status;

	/**
	 * 输入源配置
	 */
	@Column(length = 102400)
	private String readConfig;

	/**
	 * 输出源配置
	 */
	@Column(length = 102400)
	private String writeConfig;

	/**
	 * 定时任务配置
	 */
	@Column(length = 4000)
	private String scheduleConfig;

	/**
	 * 
	 */
	@Column(length = 102400)
	private String tableMeta;

	private String type;

	/**
	 * customerId
	 */
	private String token;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public long getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}

	public DataSourceType getDataSourceType() {
		return dataSourceType;
	}

	public void setDataSourceType(DataSourceType dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getReadConfig() {
		return readConfig;
	}

	public void setReadConfig(String readConfig) {
		this.readConfig = readConfig;
	}

	public String getWriteConfig() {
		return writeConfig;
	}

	public void setWriteConfig(String writeConfig) {
		this.writeConfig = writeConfig;
	}

	public String getConfigFlag() {
		return configFlag;
	}

	public void setConfigFlag(String configFlag) {
		this.configFlag = configFlag;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getScheduleConfig() {
		return scheduleConfig;
	}

	public void setScheduleConfig(String scheduleConfig) {
		this.scheduleConfig = scheduleConfig;
	}

	public DataSourceType getDataTargetType() {
		return dataTargetType;
	}

	public void setDataTargetType(DataSourceType dataTargetType) {
		this.dataTargetType = dataTargetType;
	}

	public String getTableMeta() {
		return tableMeta;
	}

	public void setTableMeta(String tableMeta) {
		this.tableMeta = tableMeta;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

}
