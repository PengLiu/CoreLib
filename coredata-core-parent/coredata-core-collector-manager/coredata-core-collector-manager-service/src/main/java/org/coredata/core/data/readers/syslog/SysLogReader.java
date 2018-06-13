package org.coredata.core.data.readers.syslog;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.coredata.core.data.DefaultRecord;
import org.coredata.core.data.OutputFieldsDeclarer;
import org.coredata.core.data.PluginConfig;
import org.coredata.core.data.Reader;
import org.coredata.core.data.Record;
import org.coredata.core.data.RecordCollector;
import org.coredata.core.data.debugger.JobDetail;
import org.coredata.core.data.debugger.JobStatus;
import org.coredata.core.data.readers.ReaderProperties;
import org.graylog2.syslog4j.server.SyslogServer;
import org.graylog2.syslog4j.server.SyslogServerConfigIF;
import org.graylog2.syslog4j.server.SyslogServerEventIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.impl.net.tcp.TCPNetSyslogServerConfig;
import org.graylog2.syslog4j.server.impl.net.udp.UDPNetSyslogServerConfig;
import org.graylog2.syslog4j.util.SyslogUtility;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service(value = "syslogReader")
@Scope("prototype")
public class SysLogReader extends Reader {

	private String ip = null;
	private String protocol = null;
	private int port = 0;
	private String serverId = null;

	private long index = 0L;
	private RecordCollector recordCollector;

	private boolean running = true;

	private long currentTime;
	
	private long readTimeout;
	
	@Override
	public void prepare(PluginConfig readerConfig) {
		super.prepare(readerConfig);
		this.port = readerConfig.getInt("port", 9999);
		this.ip = readerConfig.getString("ip", "0.0.0.0");
		this.protocol = readerConfig.getString("protocol", "udp");
		this.serverId = new StringBuffer().append(this.protocol).append("_").append(this.ip).append("_").append(this.port).toString();
		readTimeout = readerConfig.getLong(ReaderProperties.READ_TIMEOUT, 0);
	}

	@Async
	@Override
	public CompletableFuture<JobDetail> execute(RecordCollector recordCollector) {

		this.recordCollector = recordCollector;

		SyslogServerIF syslogServer = null;
		try {
			syslogServer = SyslogServer.getInstance(serverId);
		} catch (Exception e) {
			;
		}
		if (syslogServer == null) {
			if (protocol.toLowerCase().equals("udp")) {
				syslogServer = SyslogServer.createInstance(serverId, new UDPNetSyslogServerConfig());
			} else {
				syslogServer = SyslogServer.createInstance(serverId, new TCPNetSyslogServerConfig());
			}
		}

		SyslogServerConfigIF syslogServerConfig = syslogServer.getConfig();
		syslogServerConfig.setHost(ip);
		syslogServerConfig.setPort(port);

		SyslogHandler eventHandler = new SyslogHandler();
		eventHandler.setSysLogReader(this);
		syslogServerConfig.removeAllEventHandlers();
		syslogServerConfig.addEventHandler(eventHandler);

		SyslogServer.getThreadedInstance(serverId);
		currentTime = System.currentTimeMillis();
		while (running) {
			if (readTimeout > 0 && ((System.currentTimeMillis() - currentTime) / 1000 >= readTimeout)) {
				close();
			}
			SyslogUtility.sleep(1000);
		}

		jobDetail.setStatus(JobStatus.Success);
		return CompletableFuture.completedFuture(jobDetail);
	}

	public void process(SyslogServerEventIF event) {

		if (getRecordLimit() > 0) {
			if (index >= getRecordLimit()) {
				// debugger
				close();
				return;
			}
			index++;
		}
		String date = (event.getDate() == null ? new Date() : event.getDate()).toString();
		String facility = SyslogUtility.getFacilityString(event.getFacility());
		String level = SyslogUtility.getLevelString(event.getLevel());

		Record record = new DefaultRecord(5);
		record.add(date);
		record.add(facility);
		record.add(level);
		record.add(event.getHost());
		record.add(event.getMessage().replace("\n", "").replace("\r", ""));

		doFilter(record);
		// get selected columns
		doSelect(record);
		recordCollector.send(record);
	}

	@Override
	public void close() {
		running = false;
		SyslogServer.destroyInstance(serverId);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {

	}

}
