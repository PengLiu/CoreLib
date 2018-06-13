package org.coredata.core.agent.collector.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CollectService {

	@Autowired
	private EhCacheCacheManager ehCacheCacheManager;

	private static final String SUPPORT_FIX = "notsup_";

	private static final String COMMON = ",";

	private Cache cached;

	@PostConstruct
	public void initCache() {
		cached = ehCacheCacheManager.getCache("NotSupportCmdCache");
	}

	/**
	 * 该方法用于验证该命令是否为不支持命令
	 * @param instId
	 * @param cmdName
	 * @return true is support else not
	 */
	public boolean checkIfSupportCmd(String instId, String cmdName) {
		if (StringUtils.isEmpty(instId) || StringUtils.isEmpty(cmdName))
			return true;
		String cmds = cached.get(getSupportKey(instId), String.class);
		if (StringUtils.isEmpty(cmds))
			return true;
		String[] notCmds = cmds.split(COMMON);
		for (String notCmd : notCmds)
			if (notCmd.equals(cmdName))
				return false;
		return true;
	}

	private String getSupportKey(String instId) {
		return SUPPORT_FIX + instId;
	}

	/**
	 * 该方法用于向redis中插入不支持的命令集合
	 * @param instId
	 * @param cmdName
	 */
	public void insertNotSupportCmd(String instId, String cmdName) {
		StringBuilder result = new StringBuilder();
		String cmds = cached.get(getSupportKey(instId), String.class);
		if (!StringUtils.isEmpty(cmds))
			result.append(cmds);
		result.append(result.length() <= 0 ? cmdName : COMMON + cmdName);
		String resultCmds = result.toString();
		cached.put(getSupportKey(instId), resultCmds);
		//推送给coremanager存入redis
		sysNotSupportCmds(instId, resultCmds);
	}

	/**
	 * 用于同步不支持命令到coremanager
	 * @param instId
	 * @param cmdName
	 */
	private void sysNotSupportCmds(String instId, String cmdNames) {
		if (StringUtils.isEmpty(instId) || StringUtils.isEmpty(cmdNames))
			return;
		Map<String, String> cmd = new HashMap<>();
		cmd.put("action", "syscmds");
		cmd.put("instId", instId);
		cmd.put("cmds", cmdNames);
		//TODO 同步不支持命令 fire(this.session, JSON.toJSONString(cmd));
	}

	/**
	 * TODO 通信部分代码
	 */
	//	private synchronized void fire(Session session, String msg) {
	//		try {
	//			session.getBasicRemote().sendText(msg);
	//		} catch (Throwable e) {
	//			logger.error("Send data to server error ", e);
	//		}
	//	}

}
