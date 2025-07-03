package com.example.demo.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionRegistry {

    private static final long TIMEOUT_MILLIS = 5L * 60 * 1000;

    private record SessionInfo(WebSocketSession session, Instant lastActivity) {}

    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

    public void register(String userId, WebSocketSession session) {
        sessions.put(userId, new SessionInfo(session, Instant.now()));
    }

    public void unregister(String userId) {
        sessions.remove(userId);
    }

    public void updateActivity(String userId) {
        sessions.computeIfPresent(userId, (id, info) ->
                new SessionInfo(info.session(), Instant.now()));
    }

    @SuppressWarnings("resource")
    public void sendToUser(String userId, String json) {
        SessionInfo info = sessions.get(userId);
        if (info != null && info.session().isOpen()) {
            try {
                log.info("sendToUser: {}", json);
                info.session().sendMessage(new TextMessage(json));
                updateActivity(userId);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void removeInactiveSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
            boolean expired = now.toEpochMilli() - entry.getValue().lastActivity().toEpochMilli() > TIMEOUT_MILLIS;
            if (expired) {
                try {
                    entry.getValue().session().close();
                    log.info("Session for userId {} timed out and was closed.", entry.getKey());
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            return expired;
        });
    }
}
