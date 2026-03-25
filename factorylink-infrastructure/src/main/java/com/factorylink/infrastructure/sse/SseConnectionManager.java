package com.factorylink.infrastructure.sse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class SseConnectionManager {

    private static final long TIMEOUT = 30 * 60 * 1000L;

    private final Map<Long, SseUserConnection> connections = new ConcurrentHashMap<>();

    public SseEmitter createConnection(Long userId, String username, String roleKey) {
        removeConnection(userId);

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        SseUserConnection connection = new SseUserConnection(userId, username, roleKey, emitter);
        connections.put(userId, connection);

        emitter.onCompletion(() -> connections.remove(userId));
        emitter.onTimeout(() -> connections.remove(userId));
        emitter.onError(e -> connections.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("connect").data("连接成功"));
        } catch (IOException e) {
            log.warn("发送SSE连接确认失败, userId={}", userId, e);
            connections.remove(userId);
        }

        log.info("SSE连接已建立, userId={}, username={}, roleKey={}", userId, username, roleKey);
        return emitter;
    }

    public void removeConnection(Long userId) {
        SseUserConnection old = connections.remove(userId);
        if (old != null) {
            old.getEmitter().complete();
            log.info("SSE连接已关闭, userId={}", userId);
        }
    }

    public void sendToUser(Long userId, SseMessage message) {
        SseUserConnection connection = connections.get(userId);
        if (connection != null) {
            doSend(connection, message);
        }
    }

    public void sendToRole(String roleKey, SseMessage message) {
        connections.values().stream()
                .filter(c -> roleKey.equals(c.getRoleKey()))
                .forEach(c -> doSend(c, message));
    }

    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        connections.forEach((userId, connection) -> {
            try {
                connection.getEmitter().send(SseEmitter.event().name("heartbeat").data(""));
            } catch (IOException e) {
                log.debug("SSE心跳失败, 移除连接 userId={}", userId);
                connections.remove(userId);
            }
        });
    }

    private void doSend(SseUserConnection connection, SseMessage message) {
        try {
            connection.getEmitter().send(SseEmitter.event()
                    .name("message")
                    .data(message));
        } catch (IOException e) {
            log.warn("SSE消息发送失败, userId={}", connection.getUserId(), e);
            connections.remove(connection.getUserId());
        }
    }
}
