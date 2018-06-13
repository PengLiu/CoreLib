package org.coredata.core.data.schedule;

import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.coredata.core.data.JobConfig;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Record;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.entities.DataSourceType;
import org.coredata.core.data.readers.ReaderProperties;
import org.coredata.core.data.readers.jdbc.JDBCReaderProperties;
import org.coredata.core.data.schedule.ScheduleEnum.ScheduleType;
import org.coredata.core.data.service.DataSourceService;
import org.coredata.core.data.service.ImportJobService;
import org.coredata.core.data.util.SpringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class DataImportScheduleJob implements Job {

	private final Logger log = LoggerFactory.getLogger(DataImportScheduleJob.class);

	private String jobId;

	private static ImportJobService service;

	private static DataSourceService dataSourceService;

	private JobConfig jobConfig;

	private ScheduleType type;
	
	static {
		service = SpringUtils.getBean(ImportJobService.class);
		dataSourceService = SpringUtils.getBean(DataSourceService.class);
	}
	
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			log.error("[INFO] name: {} start", jobId);
			JobScheduler.getStatus().put(jobId, Status.running);
			if (type.equals(ScheduleType.Immediate)) {
				// 启动后立即执行,syslog,receive类型的任务
				PluginConfig readConifg = jobConfig.getReaderConfig();
				readConifg.remove(ReaderProperties.READ_TIMEOUT);
				dataSourceService.run(jobConfig);
			} else {
				// 定时执行的任务,如果是jdbc类型,可能存在增量/覆盖的区别,如果是增量,需要执行checkColumn,保留本次执行的最后一条记录的指定列的值
				Future<JobDetail> future = dataSourceService.run(jobConfig);
				String readConfig = checkColumn(jobId, jobConfig, future);
				if(readConfig!=null) {
					DataImportJob job = service.findById(jobId);
					job.setReadConfig(readConfig);
					service.saveJob(job);
				}
			}
		} catch (Exception e) {
			JobScheduler.getStatus().put(jobId, Status.stop);
			throw e;
		}
	}
	/**
	 * 主要用于JDBC增量获取数据,保存本次任务的最后一条记录
	 * 
	 * @param jobId
	 * @param jobConfig
	 * @param future
	 */
	private String checkColumn(String jobId, JobConfig jobConfig, Future<JobDetail> future) {
		try {
			String type = jobConfig.getReaderName();
			if (type.equals(DataSourceType.jdbcReader.name())) {
				PluginConfig readerConfig = jobConfig.getReaderConfig();
				String checkColumn = readerConfig.getString(JDBCReaderProperties.CHECK_COLUMN);
				// 如果获取到CHECK_COLUMN属性,即认为是增量获取数据
				if (StringUtils.isNotEmpty(checkColumn)) {
					Record record = null;
					JobDetail tmp = future.get();
					if (tmp.getLastVal() != null) {
						record = tmp.getLastVal();
						String checkIndex = readerConfig.getString(JDBCReaderProperties.CHECK_INDEX);
						Object lastValue = record.get(Integer.parseInt(checkIndex));
						log.error("[INFO] name: {}, checkColumn:{}, lastvalue: {}", jobId, checkColumn, lastValue);
						if (lastValue != null) {
							readerConfig.setString(JDBCReaderProperties.LAST_VAL, String.valueOf(lastValue));
							String json = JSON.toJSONString(readerConfig);
							return json;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JobConfig getJobConfig() {
		return jobConfig;
	}

	public void setJobConfig(JobConfig jobConfig) {
		this.jobConfig = jobConfig;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public static ImportJobService getService() {
		return service;
	}

	public static void setService(ImportJobService service) {
		DataImportScheduleJob.service = service;
	}

	public static DataSourceService getDataSourceService() {
		return dataSourceService;
	}

	public static void setDataSourceService(DataSourceService dataSourceService) {
		DataImportScheduleJob.dataSourceService = dataSourceService;
	}

	public ScheduleType getType() {
		return type;
	}

	public void setType(ScheduleType type) {
		this.type = type;
	}

}
