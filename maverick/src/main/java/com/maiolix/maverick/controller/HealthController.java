package com.maiolix.maverick.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller per health checks semplici
 * Fornisce endpoint di base per Docker health check
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController implements HealthIndicator {

    /**
     * Endpoint di health semplice per Docker
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "maverick-platform");
        
        log.debug("Health check requested - Status: UP");
        return ResponseEntity.ok(status);
    }

    /**
     * Endpoint di health dettagliato
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Informazioni base
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "maverick-platform");
        health.put("version", "1.0.0");
        
        // Runtime info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("max", runtime.maxMemory() / (1024 * 1024) + "MB");
        memory.put("total", runtime.totalMemory() / (1024 * 1024) + "MB");
        memory.put("free", runtime.freeMemory() / (1024 * 1024) + "MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + "MB");
        
        health.put("memory", memory);
        health.put("processors", runtime.availableProcessors());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Implementazione di HealthIndicator per Spring Boot Actuator
     */
    @Override
    public Health health() {
        // Check semplice che l'applicazione sia avviata
        try {
            return Health.up()
                    .withDetail("status", "UP")
                    .withDetail("service", "maverick-platform")
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "DOWN")
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
        }
    }
}
