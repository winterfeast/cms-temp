package com.example.demo.listener;

import com.example.demo.registry.ResponseHolder;
import com.example.demo.registry.SessionRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCommandListener {

    private static final String DEVICE_RESPONSE_COMMAND= "zigbee2mqtt/+/set";
    private static final String DEVICE_RESPONSE_DATA_SHARE = "iss_ai/hubs/+/devices/data/share/response";

    private final MqttClient client;
    private final SessionRegistry sessionRegistry;
    private final ResponseHolder responseHolder;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() throws MqttException {
        subscribeToUserCommand();
        subscribeToDeviceData();
    }

    private void subscribeToUserCommand() throws MqttException {
        client.subscribe(DEVICE_RESPONSE_COMMAND, ((topic, message) -> {
            try {
                JsonNode rootNode = objectMapper.readTree(message.getPayload());
                log.debug("Received device command: {}", rootNode.toString());

                String correlationIdStr = Optional.ofNullable(rootNode.get("correlationId"))
                        .map(JsonNode::asText)
                        .orElseThrow(() -> new IllegalArgumentException("Missing correlationId"));

                UUID correlationId = UUID.fromString(correlationIdStr);

                responseHolder.getUserNameByCorrelationId(correlationId).ifPresentOrElse(userId -> {
                    log.debug("User ID: {}", userId);
                    ((ObjectNode) rootNode).remove("correlationId");

                    try {
                        String payload = objectMapper.writeValueAsString(rootNode);
                        sessionRegistry.sendToUser(userId, payload);
                        responseHolder.unregister(correlationId);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to serialize JSON for user: {}", userId, e);
                    }
                }, () -> log.warn("No user found for correlationId: {}", correlationId));

            } catch (Exception e) {
                log.error("Failed to process MQTT message", e);
            }
        }));
    }

    private void subscribeToDeviceData() throws MqttException {
        client.subscribe(DEVICE_RESPONSE_DATA_SHARE, ((topic, message) -> {
            JsonNode rootNode = objectMapper.readTree(message.getPayload());
            log.info("Received data, topic: {}, message {}", topic, rootNode.toString());

            String correlationIdStr = Optional.ofNullable(rootNode.get("correlationId"))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> new IllegalArgumentException("Missing correlationId"));

            UUID correlationId = UUID.fromString(correlationIdStr);

            responseHolder.getUserNameByCorrelationId(correlationId).ifPresentOrElse(userId -> {
                log.debug("User ID: {}", userId);
                ((ObjectNode) rootNode).remove("correlationId");

                try {
                    String payload = objectMapper.writeValueAsString(rootNode);
                    sessionRegistry.sendToUser(userId, payload);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize JSON for user: {}", userId, e);
                }
            }, () -> log.warn("No user found for correlationId: {}", correlationId));
        }));
    }
}
