package org.coredata.core.framework.agentmanager.cmds.response;


import org.coredata.core.model.constants.ApiConstant;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 登录对应响应类
 * @author sushi
 *
 */
public class TestResponse implements Serializable {

	private static final long serialVersionUID = -669542524368714416L;

	private static final String RESULT = "result";

	private static final String FAIL_REASON = "failreason";

	private static final String TIME_OUT = "timed out";

	private static final String REQUEST_TIME_OUT = "超时";

	private static final String REFUSED = "Communications link failure";

	private static final String USER_PASSWORD_ERR = "using password: YES";

	private static final String CONNECTION_ERR = "Create connection error";

	/**
	 * 响应返回结果
	 */
	private List<Map<String, String>> results;

	/**
	 * 响应此次请求类型
	 */
	private String action;

	/**
	 * 记录Agent请求携带上来的id，用于返回给Agent时匹配对应的命令
	 */
	private String seq;

	public TestResponse() {

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

	public List<Map<String, String>> getResults() {
		return results;
	}

	public void setResults(List<Map<String, String>> results) {
		this.results = results;
	}

	/**
	 * 该方法用于翻译Agent返回的错误信息
	 * @return
	 */
	public List<Map<String, String>> getTransformResults() {
		results.forEach(r -> {
			String result = r.get(RESULT);
			if (ApiConstant.SUCCESS_FLAG.equals(result))
				return;
			String failreson = r.get(FAIL_REASON);//获取错误异常原因
			String realFail = "系统异常，请稍后重试";
			if (failreson == null) {
				r.put(FAIL_REASON, realFail);
				return;
			}
			if (failreson.contains(TIME_OUT) || failreson.contains(REQUEST_TIME_OUT))
				realFail = "请求超时";
			else if (failreson.contains(REFUSED))
				realFail = "拒绝连接，请检查连接信息";
			else if (failreson.contains(USER_PASSWORD_ERR))
				realFail = "用户名或密码错误，请检查连接信息";
			else if (failreson.contains(CONNECTION_ERR))
				realFail = "创建连接失败，请检查用户名或密码";
			r.put(FAIL_REASON, realFail);
		});
		return results;
	}
}
