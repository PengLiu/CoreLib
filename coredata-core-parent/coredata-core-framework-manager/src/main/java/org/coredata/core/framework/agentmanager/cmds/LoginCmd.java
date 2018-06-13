package org.coredata.core.framework.agentmanager.cmds;

import org.apache.log4j.Logger;

public class LoginCmd extends Command {

	private static final Logger logger = Logger.getLogger(LoginCmd.class);

	/**
	 * Agent携带上来的ip信息
	 */
	private String ip;

	/**
	 * Agent此次的动作，login，ping等
	 */
	private String action;

	/**
	 * Agent携带的证书信息
	 */
	private String credentials;

	/**
	 * Agent类型，分为standard|embedded|light
	 */
	private String type;

	/**
	 * Agent携带的描述信息
	 */
	private String info;

	/**
	 * Agent可以完成的协议，可为多个，按英文逗号分割
	 */
	private String[] features;

	/**
	 * 新增Agent相关携带信息
	 */
	private String[] tasks;

	/**
	 * 此次请求携带的id，表明此次请求的标识，近似于sessionID
	 */
	private String seq;

	/**
	 * 该命令的响应类
	 */
	private LoginResponse response;

	@Override
	public void processResult(String result) {
		//处理结果
		if (logger.isDebugEnabled())
			logger.debug("Recive Message ::: " + result);
		this.response = new LoginResponse(this, result);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCredentials() {
		return credentials;
	}

	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String[] getFeatures() {
		return features;
	}

	public void setFeatures(String[] features) {
		this.features = features;
	}

	@Override
	public String getSeq() {
		return seq;
	}

	@Override
	public void setSeq(String seq) {
		this.seq = seq;
	}

	public LoginResponse getResponse() {
		return response;
	}

	public void setResponse(LoginResponse response) {
		this.response = response;
	}

	public String[] getTasks() {
		return tasks;
	}

	public void setTasks(String[] tasks) {
		this.tasks = tasks;
	}

	/**
	 * 登录对应响应类
	 * @author sushi
	 *
	 */
	public class LoginResponse {
		/**
		 * Agent携带上来的IP信息
		 */
		private String ip;

		/**
		 * 响应返回结果
		 */
		private String result;

		/**
		 * 响应此次请求类型
		 */
		private String action;
		
		private String customerId;

		/**
		 * 记录Agent请求携带上来的id，用于返回给Agent时匹配对应的命令
		 */
		private String seq;

		public LoginResponse(LoginCmd cmd, String result) {
			this.ip = cmd.getIp();
			this.result = result;
			this.action = cmd.getAction();
			this.seq = cmd.getSeq();
			this.customerId = cmd.getCredentials();
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getSeq() {
			return seq;
		}

		public void setSeq(String seq) {
			this.seq = seq;
		}

		public String getCustomerId() {
			return customerId;
		}

		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}
	}

}
