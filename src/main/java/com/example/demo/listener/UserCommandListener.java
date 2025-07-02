package com.example.demo.listener;

import com.example.demo.registry.ResponseHolder;
import com.example.demo.registry.SessionRegistry;
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

    private static final String DEVICE_COMMAND_RESPONSE_TOPIC_TEMPLATE = "iss_ai/hubs/+/devices/+/state";

    private final MqttClient client;
    private final SessionRegistry sessionRegistry;
    private final ResponseHolder responseHolder;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() throws MqttException {
        client.subscribe(DEVICE_COMMAND_RESPONSE_TOPIC_TEMPLATE, ((topic, message) -> {
            JsonNode rootNode = objectMapper.readTree(message.getPayload());
            log.info("Received device command: {}", rootNode.toString());

            UUID correlationId = Optional.ofNullable(rootNode.get("correlationId"))
                    .map(JsonNode::asText)
                    .map(UUID::fromString)
                .orElseThrow(() -> new RuntimeException("CorrelationId is not found"));

            Optional<String> userResponse = responseHolder.getUserNameByCorrelationId(correlationId);

            if (userResponse.isPresent()) {
                String userId = userResponse.get();
                log.info("User ID: {}", userId);
                ((ObjectNode) rootNode).remove("correlationId");

                sessionRegistry.sendToUser(userId, objectMapper.writeValueAsString(rootNode));
                responseHolder.unregister(correlationId);
            }
        }));
    }
}
