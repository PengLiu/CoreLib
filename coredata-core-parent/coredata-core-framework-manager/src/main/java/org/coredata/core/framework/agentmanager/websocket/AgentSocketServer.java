package org.coredata.core.framework.agentmanager.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.coredata.core.framework.agentmanager.cmds.CallFuture;
import org.coredata.core.framework.agentmanager.cmds.Command;
import org.coredata.core.framework.agentmanager.cmds.LoginCmd;
import org.coredata.core.framework.agentmanager.config.AgentManagerConfig;
import org.coredata.core.framework.agentmanager.service.AgentManagerService;
import org.coredata.core.framework.agentmanager.service.AgentTaskService;
import org.coredata.core.framework.agentmanager.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/agentsocket", configurator = WebsocketConfigurator.class)
@Component
public class AgentSocketServer {

	private static final Logger logger = LoggerFactory.getLogger(AgentSocketServer.class);

	public static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

	/**
	 * 用于储存5s内未登录的请求连接
	 */
	public static final ConcurrentHashMap<Session, Timer> waitClose = new ConcurrentHashMap<>();

	/**
	 * 用于储存agent断开连接后的计时
	 */
	public static final ConcurrentHashMap<String, Timer> disconnect = new ConcurrentHashMap<>();

//	private Session session;

	private AgentManagerService agentService;

	private AgentTaskService agentTaskService;

//	private HttpSession httpSession;

	private ApplicationContext ctx;

	@Autowired
	private AgentManagerConfig config;

//	private ZzOutletTcpProcesser zzOutletTcpProcesser;

	/**
	 * agent和服务断开连接的超时时间
	 */
	private long AGENT_DIS_TIME_OUT = 5000;

	@PostConstruct
	public void init() {
		AGENT_DIS_TIME_OUT = config.getAgentDisconnectTimeout();
	}

	/**
	 * Agent与websocket连接时触发
	 * @param session
	 * @param config
	 * @throws IOException
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws IOException {
		logger.info("***********Connect Success=========" + session.getId());
		session.setMaxBinaryMessageBufferSize(5242800);
		session.setMaxTextMessageBufferSize(5242800);
//		this.session = session;
		HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		registServices(httpSession);
		//连接上来后，几秒之内没有请求登录就关闭连接利用java timer
		registCloseTimer(session, 5000);
	}

	/**
	 * websocket接收到Agent发送消息时触发
	 * @param session
	 * @param msg
	 */
	@OnMessage
	public void onMessage(Session session, String msg) {
		if (logger.isInfoEnabled()) {
			logger.info("Received message " + msg);
		}
		try {
			JSONObject json = JSON.parseObject(msg);
			Object seq = json.get(WebsocketConstant.SEQ);
			//如果接收到的命令在命令列表中，则设置返回值。否则认为是Agent主动请求的数据
			if (seq != null && Command.futures.containsKey(seq.toString())) {
				CallFuture future = Command.futures.get(seq.toString());
				future.setValue(msg);
			} else {
				processRequest(session,msg);
			}
		} catch (Exception e) {
			logger.error("Error parse message.", e);
			cleanTimer(session);
			closeSession(session);
		}
	}

	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("----websocket connect error,session id is " + session.getId());
		closeSession(session);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		logger.info("----client has closed,session id is " + session.getId() + "----" + reason.toString());
		closeSession(session);
	}

	/**
	 * 该方法用于处理Agent主动请求命令
	 * @param msg
	 */
	private void processRequest(Session session, String msg) {
		try {
			JSONObject json = JSON.parseObject(msg);
			String action = json.getString(WebsocketConstant.ACTION);
			if (action == null) {
                throw new Exception("Cmd has error,action is required.");
            }
			switch (action) {
			case WebsocketConstant.ACTION_LOGIN://Agent登录
				cleanTimer(session);
				LoginCmd cmd = new LoginCmd().init(msg);
				String ip = cmd.getIp();
				if (!checkAgent(ip)) {//如果Agent的ip字段不合法，则直接关闭该session
					if (logger.isDebugEnabled()) {
                        logger.debug("Agent Ip is null,close this session.");
                    }
					closeSession(session);
					return;
				}
				cleanDisconnectTimer(ip);//取消断线计时timer
				String result = agentService.processAgentLogin(cmd);
				cmd.processResult(result);
				//当登录请求并且返回成功时，将该session加入池子中
				if (WebsocketConstant.RESPONSE_OK.equals(result)) {
					sessions.put(cmd.getIp(), session);
					session.getUserProperties().put("ip", cmd.getIp());
					session.getUserProperties().put("token", cmd.getCredentials());
					//返回给Agent请求
					WebsocketUtil.sendMessageBySession(session, JSON.toJSONString(cmd.getResponse()));
					//此处更新t_agent_task表中记录
					agentTaskService.updateCollectCmd();
				} else {
					closeSession(session);
				}

				break;
			case WebsocketConstant.ACTION_TASK_REPORT:
				sessions.entrySet().forEach(entry -> {
					if (entry.getValue().equals(session)) {
						agentService.updateAgentRecord(entry.getKey());
					}
				});
				break;
			case WebsocketConstant.ACTION_SYS_CMDS:
				String instId = json.getString(WebsocketConstant.INST_ID);
				String cmds = json.getString(WebsocketConstant.NOT_SUPPORT_CMDS);
				agentService.sysNotSupportCmds(instId, cmds);
				if (logger.isDebugEnabled()) {
                    logger.debug("SysNotSupport cmds instId is:" + instId + ",cmds is:" + cmds);
                }
				break;
//			case WebsocketConstant.ACTION_ZZOUTLETTCP:
//				zzOutletTcpProcesser.dealMsg(json.toJSONString());
//				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("Cmd has error,can not process.", e);
		}
	}

	/**
	 * 该方法用于验证登录的agent合法性
	 * @param ip
	 * @return 合法 true 不合法 false
	 */
	private boolean checkAgent(String ip) {
		return !StringUtils.isEmpty(ip);
	}

	/**
	 * 该方法用于注册相关计时器
	 */
	private void registCloseTimer(Session session, long time) {
		Timer timer = new Timer(true);
		waitClose.put(session, timer);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Timer cancleTimer = waitClose.get(session);
				if (cancleTimer == null) {
                    return;
                }
				cancleTimer.cancel();
				//并且关闭session
				logger.info("----Time out.Close session id is ::: " + session.getId());
				try {
					session.close();
				} catch (IOException e) {
					logger.error(LogUtil.stackTraceToString(e));
				}
				waitClose.remove(session);
			}
		};
		timer.schedule(task, time, time);
	}

	/**
	 * 该方法用于注册相关计时器
	 */
	private void registDisconnectTimer(String ip, long time) {
		Timer timer = new Timer(true);
		disconnect.put(ip, timer);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Timer cancleTimer = disconnect.get(ip);
				if (cancleTimer == null) {
                    return;
                }
				//并且关闭session
				logger.info("----Agent has disconnected----");
				try {
					//此处开始更新相关后续操作，更新agent当前任务数
					agentTaskService.updateAgentTaskPending(ip);
					disconnect.remove(ip);
					cancleTimer.cancel();
				} catch (Exception e) {
					logger.error(LogUtil.stackTraceToString(e));
				}
			}
		};
		timer.schedule(task, time, time);
	}

	/**
	 * 该方法用于清理相关timer
	 * @param session
	 */
	private void cleanTimer(Session session) {
		Timer timer = waitClose.get(session);
		if (timer == null) {
            return;
        }
		timer.cancel();
		waitClose.remove(session);
	}

	/**
	 * 该方法用于清理断线重连的timer
	 * @param ip
	 */
	private void cleanDisconnectTimer(String ip) {
		Timer timer = disconnect.get(ip);
		if (timer == null) {
            return;
        }
		timer.cancel();
		disconnect.remove(ip);
	}

	/**
	 * 该方法用于session关闭或异常退出时的清理工作
	 */
	private void closeSession(Session session) {
		if (session == null)
			return;
		String token = session.getUserProperties().get("token") == null ? "" : session.getUserProperties().get("token").toString();
		String ip = session.getUserProperties().get("ip") == null ? "" : session.getUserProperties().get("ip").toString();
		try {
			if (session.isOpen())
				session.close();
		} catch (IOException e) {
			logger.error(LogUtil.stackTraceToString(e));
		}
		if (StringUtils.isEmpty(token) || StringUtils.isEmpty(ip))
			return;
		sessions.remove(ip);
		//更新Agent的离线时间以及在线状态
		agentService.processCloseSession(ip, token);
		//注册timer，20s后无重连，进行后续任务处理
		registDisconnectTimer(ip, AGENT_DIS_TIME_OUT);
	}

	/**
	 * 该方法用于注入所需Service，在Websocket环境下.
	 */
	private void registServices(HttpSession httpSession) {
		this.ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(httpSession.getServletContext());
		this.agentService = ctx.getBean(AgentManagerService.class);
		this.agentTaskService = ctx.getBean(AgentTaskService.class);
//		this.zzOutletTcpProcesser = ctx.getBean(ZzOutletTcpProcesser.class);
	}
}
