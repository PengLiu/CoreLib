package org.coredata.core.data.readers.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseServer {

	public static Map<Integer, HttpServer> httpServers = new ConcurrentHashMap<>();

	public static final int SERVER_START = 1;

	/**
	 * 服务端口
	 */
	protected int port;

	/**
	 * 开启服务方法
	 * @throws Exception
	 */
	protected abstract void start() throws Exception;

	/**
	 * 关闭服务方法
	 * @param port
	 * @throws Exception
	 */
	protected abstract void stop(int port) throws Exception;

}
