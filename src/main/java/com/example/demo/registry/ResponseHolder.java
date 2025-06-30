package com.example.demo.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResponseHolder {

    private final Map<UUID, String> responses = new ConcurrentHashMap<>();

    public void register(UUID correlationId, String userName) {
        responses.put(correlationId, userName);
    }

    public void unregister(UUID correlationId) {
        responses.remove(correlationId);
    }

    public Optional<String> getUserNameByCorrelationId(UUID correlationId) {
        String userName = responses.get(correlationId);
        if (userName == null) {
            return Optional.empty();
        }
        return Optional.of(userName);
    }
}
