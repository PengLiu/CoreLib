package org.coredata.core.agent.collector;

import java.util.List;

public class CmdResult {

	private String val;

	private Cmd cmd;

	private Throwable err;

	public CmdResult(String val, Cmd cmd) {
		this.val = val;
		this.cmd = cmd;
	}

	public CmdResult(Throwable err, Cmd cmd) {
		this.err = err;
		this.cmd = cmd;
	}

	public String getVal() {
		return val;
	}

	public List<Long> getTaskTimes() {
		return cmd.getTasktimes();
	}

	public Cmd getCmd() {
		return cmd;
	}

	public Throwable getErr() {
		return err;
	}

	@Override
	public String toString() {
		return "CmdResult [val=" + val + ", cmd=" + cmd.getName() + ", err=" + err + "]";
	}
}