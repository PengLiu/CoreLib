package org.coredata.core.agent.collector.exception;

import java.text.MessageFormat;

public class ProtocolException extends Exception {

	private static final long serialVersionUID = -8750933108179940440L;

	// ConnErr 连接异常, AuthErr 身份认证异常，CollErr 采集异常, PermissionErr 权限异常, UnknowErr
	// 未知异常（限制使用）
	public enum Type {
		ProtocolErr, ConnErr, AuthErr, CollErr, PermissionErr, UnKnowErr
	}

	public enum ErrNum {
		Conn_Refuse, Conn_Timeout, Auth_Err, Coll_Timeout, Coll_CMD_Err, Coll_Permission_Err, Coll_Syntax_Err
	}

	private Type type;

	private ErrNum num;

	// 对应i18n文件的key
	private String errCode;

	// 异常原始数据
	private String errSrc;

	// 命令语句
	private String cmd;

	public ProtocolException(Type type, ErrNum num, String errCode, String cmd, Exception e) {
		this.type = type;
		this.num = num;
		this.errCode = errCode;
		this.cmd = cmd;
		this.errSrc = MessageFormat.format(errCode, e!=null?e.getMessage():"") + "[" + this.cmd + "]";
	}

	public ProtocolException(Type type, String err) {
		this.type = type;
		this.errSrc = err;
	}
	
	public ProtocolException(Type type, String err, Throwable cause) {
		super(cause);
		this.type = type;
		this.errSrc = err;
	}

	public Type getType() {
		return type;
	}

	public ErrNum getNum() {
		return num;
	}

	public String getErrSrc() {
		return errSrc;
	}

	@Override
	public String toString() {
		return "ProtocolException [type=" + type + ", num=" + num + ", errCode=" + errCode + ", errSrc=" + errSrc + "]";
	}
}
