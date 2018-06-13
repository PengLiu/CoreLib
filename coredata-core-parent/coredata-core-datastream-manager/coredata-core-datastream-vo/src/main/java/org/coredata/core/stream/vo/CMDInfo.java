package org.coredata.core.stream.vo;

public class CMDInfo {

	private String alias;

	private String cmd;

	/**
	 * 命令来源，有cmd和property两种
	 */
	private String sourceType;

	public CMDInfo(String cmd, String alias) {
		this.cmd = cmd;
		this.alias = alias;
	}

	public CMDInfo(String cmd) {
		this.cmd = cmd;
	}

	public String getAlias() {
		return alias;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	@Override
	public String toString() {
		return "CMDInfo [alias=" + alias + ", cmd=" + cmd + ", sourceType=" + sourceType + "]";
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public static enum SourceType {
		cmd, property, conninfo
	}

}