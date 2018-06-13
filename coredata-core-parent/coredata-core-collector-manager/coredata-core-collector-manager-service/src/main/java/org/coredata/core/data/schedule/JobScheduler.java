package org.coredata.core.data.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.coredata.core.data.JobConfig;
import org.coredata.core.data.schedule.ScheduleEnum.ScheduleType;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * 定时任务
 * 
 * @author alis1
 *
 */
public class JobScheduler {

	private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);
	private static JobScheduler instance = new JobScheduler();
	private static Scheduler scheduler;
	private static String group = "group1";

	private JobScheduler() {

	}

	private static ConcurrentMap<String, Status> status = new ConcurrentHashMap<String, Status>();

	public static ConcurrentMap<String, Status> getStatus() {
		return status;
	}

	public static JobScheduler getInstance() throws SchedulerException {
		try {
			if (scheduler == null) {
				scheduler = new StdSchedulerFactory().getScheduler();
			}
			if (!scheduler.isStarted() || scheduler.isShutdown()) {
				scheduler.start();
			}
		} catch (SchedulerException e) {
			throw e;
		}
		return instance;
	}

	public void scheduleJob(Class<? extends Job> tClass, String jobId, JobConfig jobConfig, String scheduleDef) throws SchedulerException {
		//如果存在已执行的任务,先删除
		boolean isDel = deleteJob(jobId);
		if(log.isInfoEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("{before schedule del job:").append(jobId).append(" result:").append(isDel);
			log.info(sb.toString());
		}
		Gson gson = new Gson();
		ScheduleConfig scheduleConfig = gson.fromJson(scheduleDef, ScheduleConfig.class);
		if(scheduleConfig.getType()==null) {
			return;
		}
		JobScheduler.getStatus().put(jobId, Status.running);
		if (scheduleConfig.isImmediate()) {
			// Future<JobDetail> future = dataSourceService.runETLJob(jobConfig);
			doImmediate(tClass, jobId, jobConfig, scheduleConfig.getType());
		} else {
			String cronExpression = scheduleConfig.getCronExpression();
			doCron(tClass, cronExpression, jobId, jobConfig, scheduleConfig.getType());
		}
	}

	/**
	 * 启动后立即执行的定时任务
	 * 
	 * @param tClass
	 *            job类型
	 * @param cronExpression
	 *            定时表达式
	 * @param jobId
	 *            任务id
	 * @param jobConfig
	 *            任务配置信息
	 * @param type
	 *            任务类型(立即执行/定时执行)
	 * @throws SchedulerException 
	 */
	private void doCron(Class<? extends Job> tClass, String cronExpression, String jobId, JobConfig jobConfig,
			ScheduleType type) throws SchedulerException {
		try {
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put("jobConfig", jobConfig);
			jobDataMap.put("jobId", jobId);
			jobDataMap.put("type", type);

			JobDetail job1 = JobBuilder.newJob(tClass).withIdentity(jobId, group).usingJobData(jobDataMap).build();
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, -1);
			Trigger trigger1 = (CronTriggerImpl) TriggerBuilder.newTrigger().withIdentity(jobId, group)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).startAt(cal.getTime()).build();
			scheduler.scheduleJob(job1, trigger1);
		} catch (SchedulerException e) {
			throw e;
		}
	}

	/**
	 * 启动后立即执行且只执行一次的任务
	 * 
	 * @param tClass
	 *            job类型
	 * @param jobId
	 *            任务id
	 * @param jobConfig
	 *            任务配置信息
	 * @param type
	 *            任务类型(立即执行/定时执行)
	 * @throws SchedulerException 
	 */
	private void doImmediate(Class<? extends Job> tClass, String jobId, JobConfig jobConfig, ScheduleType type) throws SchedulerException {
		try {
			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put("jobConfig", jobConfig);
			jobDataMap.put("jobId", jobId);
			jobDataMap.put("type", type);
			JobDetail job1 = JobBuilder.newJob(tClass).withIdentity(jobId, group).usingJobData(jobDataMap).build();
			SimpleTriggerImpl st = new SimpleTriggerImpl();
			st.setKey(new TriggerKey(jobId, group));
			st.setStartTime(new Date());
			st.setRepeatCount(0);
			st.setRepeatInterval(1000L);
			scheduler.scheduleJob(job1, st);
		} catch (SchedulerException e) {
			throw e;
		}
	}

	/**
	 * 暂停任务
	 * 
	 * @param jobId
	 * @throws SchedulerException
	 */
//	public void pauseJob(String jobId) throws SchedulerException {
//		scheduler.pauseJob(getKey(jobId));
//		status.put(jobId, Status.pause);
//	}

	/**
	 * 恢复任务
	 * 
	 * @param jobId
	 * @throws SchedulerException
	 */
//	public void resumeJob(String jobId) throws SchedulerException {
//		scheduler.resumeJob(getKey(jobId));
//		status.put(jobId, Status.waiting);
//	}

	/**
	 * 暂停所有
	 * 
	 * @throws SchedulerException
	 */
//	public void pauseAll() throws SchedulerException {
//		scheduler.pauseAll();
//		for (String jobId : status.keySet()) {
//			status.put(jobId, Status.pause);
//		}
//	}

	/**
	 * 恢复所有
	 * 
	 * @throws SchedulerException
	 */
//	public void resumeAll() throws SchedulerException {
//		scheduler.resumeAll();
//		for (String jobId : status.keySet()) {
//			status.put(jobId, Status.waiting);
//		}
//	}

	/**
	 * 删除任务
	 * 
	 * @param jobId
	 * @throws SchedulerException
	 */
	public boolean deleteJob(String jobId) throws SchedulerException {
		boolean result = scheduler.deleteJob(getKey(jobId));
		status.remove(jobId);
		return result;
	}

	/**
	 * 删除多个任务
	 * 
	 * @param jobs
	 * @throws SchedulerException
	 */
	public void deleteJob(List<String> jobs) throws SchedulerException {
		if (jobs != null && !jobs.isEmpty()) {
			List<JobKey> keys = new ArrayList<JobKey>();
			for (String jobId : jobs) {
				keys.add(getKey(jobId));
			}
			scheduler.deleteJobs(keys);
			for (String jobId : jobs) {
				status.remove(jobId);
			}
		}
	}

	private JobKey getKey(String jobId) {
		return new JobKey(jobId, group);
	}

}
