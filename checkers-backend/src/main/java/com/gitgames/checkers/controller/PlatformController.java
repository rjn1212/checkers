package com.gitgames.checkers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/platform")
public class PlatformController {

    private final Instant startupTime = Instant.now();

    @GetMapping("/health")
    public Map<String, Object> health() {
        long uptime = Duration.between(startupTime, Instant.now()).getSeconds();
        return Map.of(
                "status", "ok",
                "game", "checkers",
                "version", "1.0.0",
                "uptime", uptime
        );
    }
}
