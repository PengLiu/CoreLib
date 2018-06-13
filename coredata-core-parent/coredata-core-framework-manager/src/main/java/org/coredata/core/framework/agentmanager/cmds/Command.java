package org.coredata.core.framework.agentmanager.cmds;

import com.alibaba.fastjson.JSON;
import org.coredata.core.framework.agentmanager.websocket.WebsocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public abstract class Command {

	private static Logger logger = LoggerFactory.getLogger(Command.class);

	protected static final String PROTOCOL_FLAG = "protocol";

	public static final ConcurrentMap<String, CallFuture> futures = new ConcurrentHashMap<>();

	private String seq = UUID.randomUUID().toString();

	public String cmd;

	/**
	 * 发送给Agent携带的连接信息
	 */
	private List<Map<String, String>> connections = new ArrayList<>();

	/**
	 * 此次命令的返回结果
	 */
	private String result;

	public void processResult(String result) {

	}

	public Future<String> execute(String ip, long timeoutInMs) throws CommandException {
		try {
			CallFuture future = new CallFuture(seq, timeoutInMs);
			futures.put(seq, future);
			if (logger.isInfoEnabled())
				logger.info(getCmd());
			//logger.info("Send Agent Cmd is :" + getCmd());
			WebsocketUtil.sendMessage(getCmd(), ip);
			return future;
		} catch (Exception e) {
			futures.remove(seq);
			throw new CommandException("Send data to agent error", e);
		}
	}

	/**
	 * 同步调用方法，无超时时间
	 * @param socket
	 * @return
	 * @throws Exception
	 */
	public String executeSync(String ip) throws CommandException {
		try {
			Future<String> future = execute(ip, Long.MAX_VALUE);
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new CommandException("Send data to agent error", e);
		}
	}

	/**
	 * 同步调用方法，带超时时间
	 * @param socket
	 * @return
	 * @throws TimeoutException
	 * @throws Exception
	 */
	public String executeSyncTimeOut(String ip, long time, TimeUnit unit) throws CommandException {
		try {
			Future<String> future = execute(ip, Long.MAX_VALUE);
			return future.get(time, unit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new CommandException("Send data to agent error", e);
		}
	}

	/**
	 * 异步执行命令方法，调用后直接返回
	 */
	public void executeAsyncCmd(String ip) {
		if (logger.isInfoEnabled())
			logger.info("Send Agent Cmd is :" + getCmd());
		WebsocketUtil.sendMessage(getCmd(), ip);
	}

	/**
	 * 初始化命令，将命令转成相关对象
	 * @param cmd
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Command> T init(String cmd) {
		try {
			return (T) JSON.parseObject(cmd, this.getClass());
		} catch (Exception e) {
			logger.error("Parse command error", e);
			return null;
		}
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public List<Map<String, String>> getConnections() {
		return connections;
	}

	public void setConnections(List<Map<String, String>> connections) {
		this.connections = connections;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
