package org.coredata.core.framework.agentmanager.cmds;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * 用于向Agent发送命令
 * @author sushi
 *
 */
public class CallFuture implements Future<String> {

	private static Logger logger = Logger.getLogger(CallFuture.class);

	private static enum State {
		WAITING, DONE, CANCELLED
	}

	private volatile State state = State.WAITING;

	private String seq;

	private long timeoutInMs = 0;

	private final BlockingQueue<String> response = new LinkedBlockingQueue<>(1);

	public CallFuture(String seq, long timeoutInMs) {
		this.seq = seq;
		this.timeoutInMs = timeoutInMs;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		state = State.CANCELLED;
		return true;
	}

	@Override
	public boolean isCancelled() {
		return state == State.CANCELLED;
	}

	@Override
	public boolean isDone() {
		return state == State.DONE;
	}

	@Override
	public String get() throws InterruptedException, ExecutionException {
		return response.take();
	}

	@Override
	public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		final String replyOrNull = response.poll(timeout, unit);
		if (replyOrNull == null) {
			throw new TimeoutException("Agents Time out.");
		}
		return replyOrNull;
	}

	public String getSeq() {
		return seq;
	}

	public long getTimeoutInMs() {
		return timeoutInMs;
	}

	public boolean setValue(String result) {
		try {
			response.put(result);
			state = State.DONE;
			return true;
		} catch (InterruptedException e) {
			logger.error("Set result to future task interrupted.", e);
			return false;
		}
	}

}
