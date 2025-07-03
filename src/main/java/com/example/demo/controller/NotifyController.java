package com.example.demo.controller;

import com.example.demo.registry.SessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
public class NotifyController {

    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper ;

    @PostMapping("/{userId}")
    public ResponseEntity<String> notifyUser(@PathVariable String userId,
                                             @RequestBody Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            sessionRegistry.sendToUser(userId, json);
            return ResponseEntity.ok("Message sent to " + userId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send: " + e.getMessage());
        }
    }
}
