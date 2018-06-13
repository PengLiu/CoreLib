package org.coredata.core.framework.agentmanager.websocket;

import org.apache.log4j.Logger;
import org.coredata.core.framework.agentmanager.util.LogUtil;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WebsocketUtil {

    private static Logger log = Logger.getLogger(WebsocketUtil.class);

    /**
     * 该方法用于向websocket客户端传递消息
     *
     * @param ips
     * @param msg
     */
    public synchronized static void sendMessage(String msg, String... ips) {

        List<Session> sendSessions = arrangeSession(ips);

        if (sendSessions.size() == 0) {
            return;
        }

        for (Session session : sendSessions) {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (Throwable e) {
                log.error(LogUtil.stackTraceToString(e));
            }
        }

    }

    /**
     * 该方法用于整理session，如果ip传来为空，则给全部客户端发送消息
     *
     * @param ips
     */
    public static List<Session> arrangeSession(String... ips) {
        List<Session> resultSessions = new ArrayList<>();
        if (ips == null || ips.length <= 0) {
            Collection<Session> sessions = AgentSocketServer.sessions.values();
            resultSessions.addAll(sessions);
            return resultSessions;
        }
        for (String ip : ips) {
            Session session = AgentSocketServer.sessions.get(ip);
            if (session == null) {
                continue;
            }
            resultSessions.add(session);
        }
        return resultSessions;
    }

    public static void sendMessageBySession(Session session, String msg) {
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            log.error(LogUtil.stackTraceToString(e));
        }
    }

}
