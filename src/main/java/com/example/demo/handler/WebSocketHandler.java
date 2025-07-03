package com.example.demo.handler;

import com.example.demo.listener.UserCommandPublisher;
import com.example.demo.registry.SessionRegistry;
import com.example.demo.util.MobileUserCommand;
import com.example.demo.util.UserCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_KEY = "userId";

    private final SessionRegistry sessionRegistry;
    private final UserCommandPublisher publisher;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String userId = (String) session.getAttributes().get(USER_ID_KEY);

        log.debug("added userId:{}", userId);

        if (userId != null) {
            sessionRegistry.register(userId, session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userId = (String) session.getAttributes().get(USER_ID_KEY);
        if (userId != null) {
            sessionRegistry.updateActivity(userId);
        }

        MobileUserCommand mobileUserCommand;
        try {
            mobileUserCommand = objectMapper.readValue(message.getPayload(), MobileUserCommand.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error parsing JSON", e);
        }
        UUID correlationId = UUID.randomUUID();
        UserCommand userCommand = new UserCommand(correlationId, mobileUserCommand);

        publisher.publish(userCommand, userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get(USER_ID_KEY);
        if (userId != null) {
            sessionRegistry.unregister(userId);
        }
    }
}
