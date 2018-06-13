package org.coredata.core.data.readers.syslog;

import java.net.SocketAddress;

import org.graylog2.syslog4j.server.SyslogServerEventIF;
import org.graylog2.syslog4j.server.SyslogServerIF;
import org.graylog2.syslog4j.server.SyslogServerSessionEventHandlerIF;

public class SyslogHandler implements SyslogServerSessionEventHandlerIF {

	private static final long serialVersionUID = 3591535088854214022L;

	private SysLogReader sysLogReader;

	@Override
	public void initialize(SyslogServerIF syslogServer) {
	}

	@Override
	public void destroy(SyslogServerIF syslogServer) {

	}

	@Override
	public Object sessionOpened(SyslogServerIF syslogServer, SocketAddress socketAddress) {
		return null;
	}

	@Override
	public void event(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, SyslogServerEventIF event) {
		if (sysLogReader != null) {
			sysLogReader.process(event);
		}
	}

	@Override
	public void exception(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, Exception exception) {

	}

	@Override
	public void sessionClosed(Object session, SyslogServerIF syslogServer, SocketAddress socketAddress, boolean timeout) {

	}

	public void setSysLogReader(SysLogReader sysLogReader) {
		this.sysLogReader = sysLogReader;
	}

}
