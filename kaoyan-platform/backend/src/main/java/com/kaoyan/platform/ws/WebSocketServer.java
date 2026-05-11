package com.kaoyan.platform.ws;

import com.kaoyan.platform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {
    private static final Map<Long, Session> ONLINE_USERS = new ConcurrentHashMap<>();

    private static JwtUtil jwtUtil;
    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) { WebSocketServer.jwtUtil = jwtUtil; }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        ONLINE_USERS.put(userId, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        ONLINE_USERS.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
    }

    public static void sendToUser(Long userId, String message) {
        Session session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            try { session.getBasicRemote().sendText(message); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }
}
