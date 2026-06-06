package com.reese.transactiontrackingapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/instance")
    public Map<String, String> getInstanceMetadata() {
        String hostName = System.getenv().getOrDefault("HOSTNAME", "local-dev");
        return Map.of("instance", hostName);
    }
}
