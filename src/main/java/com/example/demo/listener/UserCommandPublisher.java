package com.example.demo.listener;

import com.example.demo.registry.ResponseHolder;
import com.example.demo.util.UserCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCommandPublisher {

    private static final String DEVICE_COMMAND_SEND_TEMPLATE = "iss_ai/hubs/%s/devices/%s/command";

    private final ResponseHolder responseHolder;
    private final ObjectMapper objectMapper;
    private final MqttClient client;

    public void publish(UserCommand command, String userName) {
        String jsonCommand;
        try {
            jsonCommand = objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize command", e);
            return;
        }
        MqttMessage message = new MqttMessage(jsonCommand.getBytes());
        message.setQos(1);
        message.setRetained(false);
        String topic = String.format(DEVICE_COMMAND_SEND_TEMPLATE, command.hubId(), command.deviceId());
        try {
            client.publish(topic, message);
            responseHolder.register(command.correlationId(), userName);
            log.info("Published message to topic {}, body {}", topic, message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
