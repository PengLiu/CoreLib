package org.coredata.core.data.readers.http;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Service(value = "httpReader")
@Scope("prototype")
public class ReceiveReader extends Reader {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveReader.class);

	private int port = 0;

	private String requestMethod;

	private RecordCollector recordCollector;

	private HttpServer server;

	private final BlockingQueue<Object> mqueue = new LinkedBlockingQueue<>();

	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	private boolean running = true;

	@Override
	public void prepare(PluginConfig readerConfig) {
		super.prepare(readerConfig);
		this.port = readerConfig.getInt("port", 8080);
		this.requestMethod = readerConfig.getString("method", "POST");
		// 此处准备http服务
		HttpChannelInitializer initializer = new HttpChannelInitializer(this.mqueue, this.requestMethod);
		this.server = new HttpServer(this.port, initializer);
	}
	
	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {
		this.recordCollector = recordCollector;
		executor.execute(new ReceiveConsumer(this, this.mqueue));
		// 启动http服务
		try {
			this.server.start();
			while (running) {
				Thread.sleep(1000);
			}
			jobDetail.setStatus(JobStatus.Success);
		} catch (Exception e) {
			logger.error("Start http server error.", e);
			jobDetail.setStatus(JobStatus.JobErr);
		}
		return CompletableFuture.completedFuture(jobDetail);
	}

	@Override
	public void close() {
		executor.shutdownNow();
		try {
			this.server.stop(this.port);
		} catch (Exception e) {
			logger.error("Stop http server error.", e);
		}
		this.running = false;
	}

	// 处理接收到的数据信息
	public void process(JSONArray msg) {
		Iterator<Object> it = msg.iterator();
		while (it.hasNext()) {
			JSONObject ob = (JSONObject) it.next();
			process(ob);
		}
	}

	// 处理接收到的数据信息
	public void process(JSONObject msg) {
		int totalCol = msg.size();
		Record record = new DefaultRecord(totalCol);
		for (Map.Entry<String, Object> entry : msg.entrySet()) {
			record.add(entry.getValue());
		}
		doFilter(record);
		// get selected columns
		doSelect(record);
		if (logger.isDebugEnabled())
			logger.debug("Send HDFS record is : " + record.toString());
		recordCollector.send(record);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}
