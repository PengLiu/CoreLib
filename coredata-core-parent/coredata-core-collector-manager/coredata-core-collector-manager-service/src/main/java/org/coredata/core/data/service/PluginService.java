package org.coredata.core.data.service;

import java.util.UUID;
import java.util.concurrent.Future;

import org.coredata.core.data.DefaultJobConfig;
import org.coredata.core.data.JobConfig;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.entities.DataImportJob;
import org.coredata.core.data.entities.DataSourceType;
import org.coredata.core.data.schedule.DataImportScheduleJob;
import org.coredata.core.data.schedule.JobScheduler;
import org.coredata.core.data.schedule.ScheduleConfig;
import org.coredata.core.data.schedule.Status;
import org.coredata.core.data.util.JsonStringUtil;
import org.coredata.core.data.vo.TableMeta;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

@Service
public class PluginService {

	@Autowired
	private DataSourceService dataSourceService;

	@Autowired
	private ImportJobService service;


	public JobDetail getData(DataImportJob job) throws Exception {
		JobDetail result = null;
		DataSourceType ds = job.getDataSourceType();
		boolean isExist = false;
		Future<JobDetail> future = null;
		String debugId = UUID.randomUUID().toString();
		try {
			String configDef = job.getReadConfig();
			DataSourceType type = job.getDataSourceType();
			PluginConfig readerConfig = JSON.parseObject(configDef, PluginConfig.class);
			PluginConfig debugConfig = new PluginConfig();
			debugConfig.put("id", debugId);
			JobConfig jobConfig = new DefaultJobConfig(type.name(), readerConfig, "debugWriter", debugConfig);
			if (ds.equals(DataSourceType.syslogReader) || ds.equals(DataSourceType.httpReader)) {
				Status status = JobScheduler.getStatus().get(job.getJobId());
				if (status != null && status.equals(Status.running)) {
					// ===================特殊处理,syslog和httpserver需要开启端口,所以需要先关闭已有的任务,getmeta后重新执行
					JobScheduler.getInstance().deleteJob(job.getJobId());
					isExist = true;
				}
			}
			future = dataSourceService.run(jobConfig);
			JobDetail tmp = future.get();
			if (tmp != null && tmp.getRecords() != null) {
				result = tmp;
			}
		} catch (Exception e) {
			if (future != null) {
				if (!future.isDone()) {
					future.cancel(true);
				}
			}
			throw e;
		} finally {
			if (isExist) {
				runSchedule(job);
			}
		}
		return result;
	}

	public void runSchedule(DataImportJob job) throws Exception {
		String jobId = job.getJobId();
		JobConfig jobConfig = getJobConfig(job);
		String scheduleDef = job.getScheduleConfig();
		if (JsonStringUtil.isNotEmpty(scheduleDef)) {
			Gson gson = new Gson();
			ScheduleConfig scheduleConfig = gson.fromJson(scheduleDef, ScheduleConfig.class);
			if (scheduleConfig.getType() != null) {
				JobScheduler.getInstance().scheduleJob(DataImportScheduleJob.class, jobId, jobConfig, scheduleDef);
			} else {
				// 只执行一次且不再执行
				dataSourceService.run(jobConfig);
			}
		} else {
			// 只执行一次且不再执行
			dataSourceService.run(jobConfig);
		}
	}

	public void initScheduleJobs() {
		Iterable<DataImportJob> jobs = service.findAll();
		if (jobs != null) {
			for (DataImportJob job : jobs) {
				initScheduleJob(job);
			}
		}
	}

	private void initScheduleJob(DataImportJob job) {
		String scheduleDef = job.getScheduleConfig();
		if (JsonStringUtil.isNotEmpty(scheduleDef)) {
			try {
				String jobId = job.getJobId();
				JobConfig jobConfig = getJobConfig(job);
				JobScheduler.getInstance().scheduleJob(DataImportScheduleJob.class, jobId, jobConfig, scheduleDef);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
	}

	private JobConfig getJobConfig(DataImportJob job) {
		JobConfig jobConfig = null;
		DataSourceType readType = job.getDataSourceType();
		String readDef = job.getReadConfig();
		PluginConfig readerConfig = JSON.parseObject(readDef, PluginConfig.class);
		DataSourceType writeType = job.getDataTargetType();
		String writeDef = job.getWriteConfig();
		PluginConfig writerConfig = JSON.parseObject(writeDef, PluginConfig.class);
		String tableDef = job.getTableMeta();
		TableMeta tableMeta = JSON.parseObject(tableDef, TableMeta.class);
		jobConfig = new DefaultJobConfig(readType.name(), readerConfig, writeType.name(), writerConfig, tableMeta, job.getToken());
		// if (!readType.equals(DataSourceType.binaryReader)) {
		// DataSourceType writeType = job.getDataTargetType();
		// String writeDef = job.getWriteConfig();
		// PluginConfig writerConfig = JSON.parseObject(writeDef, PluginConfig.class);
		// jobConfig = new DefaultJobConfig(readType.name(), readerConfig,
		// writeType.name(), writerConfig);
		// } else {
		// DataSourceType writeType = job.getDataSourceType();
		// if(writeType.equals(DataSourceType.hdfsWriter)) {
		// // ===================特殊处理=======================
		// String writeDef = job.getWriteConfig();
		// PluginConfig writeConfig = JSON.parseObject(writeDef, PluginConfig.class);
		// String path = writeConfig.getString(HDFSWriterProperties.PATH);
		// String user = writeConfig.getString(HDFSWriterProperties.HADOOP_USER);
		// String uri = writeConfig.getString(HDFSWriterProperties.HDFS_URI);
		// String appendMode = writeConfig.getString(HDFSWriterProperties.WRITE_MODE);
		// boolean append = false;
		// if (StringUtils.isNotEmpty(appendMode)) {
		// append = Boolean.parseBoolean(appendMode);
		// }
		// readerConfig.put(BinaryReaderProperties.HDFS_DIR, path);
		// readerConfig.put(BinaryReaderProperties.HDFS_USER, user);
		// readerConfig.put(BinaryReaderProperties.HDFS_URI, uri);
		// readerConfig.put(HDFSWriterProperties.WRITE_MODE, append);
		//
		// // 二进制文件ftp一次导入,不用设置writeConfig
		// jobConfig = new DefaultJobConfig(readType.name(), readerConfig,
		// readType.name(), new PluginConfig());
		// }
		// }
		return jobConfig;
	}

}
