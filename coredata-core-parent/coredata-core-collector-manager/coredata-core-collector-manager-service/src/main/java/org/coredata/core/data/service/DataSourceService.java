package org.coredata.core.data.service;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.coredata.core.data.DefaultRecordCollector;
import org.coredata.core.data.JobConfig;
import org.coredata.core.data.JobContext;
import org.coredata.core.data.Metric;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.RecordEvent;
import org.coredata.core.data.RecordEventExceptionHandler;
import org.coredata.core.data.RecordWorkerHandler;
import org.coredata.core.data.WaitStrategyFactory;
import org.coredata.core.data.Writer;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.coredata.core.data.util.Utils;
import org.coredata.core.data.vo.TableMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

@Service
@Transactional(readOnly = true)
public class DataSourceService implements ApplicationContextAware {

	private long sleepMillis = 500;

	private Logger logger = LoggerFactory.getLogger(DataSourceService.class);

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Async
	public CompletableFuture<JobDetail> run(JobConfig jobConfig) {
		try {
			final Metric metric = new Metric();

			final PluginConfig readerConfig = jobConfig.getReaderConfig();
			final PluginConfig writerConfig = jobConfig.getWriterConfig();
			final TableMeta tableMeta = jobConfig.getTableMeta();

			String readerName = jobConfig.getReaderName();
			String writerName = jobConfig.getWriterName();
			String token = jobConfig.getToken();

			Reader reader = (Reader) applicationContext.getBean(readerName);
			reader.prepare(readerConfig);

			Writer writer = (Writer) applicationContext.getBean(writerName);
			writer.prepare(writerConfig, tableMeta, token);

			int bufferSize = 16384;
			WaitStrategy waitStrategy = WaitStrategyFactory.build("com.lmax.disruptor.BlockingWaitStrategy");

			JobContext context = new JobContext();

			ProducerType producerType = ProducerType.SINGLE;
			Disruptor<RecordEvent> disruptor = new Disruptor<>(RecordEvent.FACTORY, bufferSize,
					Executors.defaultThreadFactory(), producerType, waitStrategy);
			disruptor.setDefaultExceptionHandler(new RecordEventExceptionHandler(disruptor, context));
			disruptor.handleEventsWithWorkerPool(new RecordWorkerHandler[] { new RecordWorkerHandler(writer, metric) });
			RingBuffer<RecordEvent> ringBuffer = disruptor.start();

			metric.setReaderStartTime(System.currentTimeMillis());
			metric.setWriterStartTime(System.currentTimeMillis());

			Future<JobDetail> future = reader.execute(new DefaultRecordCollector(disruptor, metric));

			while (!future.isDone()) {
				if (context.isWriterError()) {
					logger.error("Write error, Closing reader and writer." + jobConfig);
					reader.close();
					writer.close();
				}
				Utils.sleep(sleepMillis);
			}

			context.setReaderFinished(true);
			metric.setReaderEndTime(System.currentTimeMillis());

			while (!isEmpty(ringBuffer)) {
				if (context.isWriterError()) {
					writer.close();
				}
				Utils.sleep(sleepMillis);
			}

			disruptor.shutdown();
			writer.close();

			metric.setWriterEndTime(System.currentTimeMillis());
			context.setWriterFinished(true);

			double readSeconds = (metric.getReaderEndTime() - metric.getReaderStartTime()) / 1000d;
			double writeSeconds = (metric.getWriterEndTime() - metric.getWriterStartTime()) / 1000d;

			final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

			String readSpeed = decimalFormat.format(metric.getReadCount().get() / readSeconds);
			String writeSpeed = decimalFormat.format(metric.getWriteCount().get() / writeSeconds);

			JobDetail jobDetail = new JobDetail();

			jobDetail.setReaderCount(metric.getReadCount().get());
			jobDetail.setWriterCount(metric.getWriteCount().get());
			jobDetail.setReaderSpentMs(decimalFormat.format(readSeconds) + "s");
			jobDetail.setWriterSpentMs(decimalFormat.format(writeSeconds) + "s");
			jobDetail.setReaderSpeed(readSpeed + "/s");
			jobDetail.setWriterSpeed(writeSpeed + "/s");
			jobDetail.setStatus(JobStatus.Success);
			jobDetail.setLastVal(metric.getLastVal());
			jobDetail.getRecords().addAll(metric.getPreview());
			jobDetail.setFields(reader.getFields());

			return CompletableFuture.completedFuture(jobDetail);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private boolean isEmpty(RingBuffer<RecordEvent> ringBuffer) {
		return ringBuffer.remainingCapacity() == ringBuffer.getBufferSize();
	}
}
