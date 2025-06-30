package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;

public record MobileUserCommand (
    String hubId,
    String deviceId,
    JsonNode payload
){ }
