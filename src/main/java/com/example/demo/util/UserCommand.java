package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record UserCommand (
    UUID correlationId,
    String hubId,
    String deviceId,
    JsonNode payload
) {

    public UserCommand(UUID correlationId, MobileUserCommand command) {
        this(correlationId, command.hubId(), command.deviceId(), command.payload());
    }
}