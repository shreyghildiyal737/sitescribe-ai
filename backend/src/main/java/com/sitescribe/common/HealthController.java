package com.sitescribe.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lets the hosting platform tell a booted service from a dead one. It says nothing
 * about tenants, versions or configuration, so it is safe to leave unauthenticated.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
