package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;

public record GlobalCommand(
        CommandType type,
        String hubName,
        JsonNode details
) { }
