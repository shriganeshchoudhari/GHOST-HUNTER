package com.ghosthunter.controller;

import com.ghosthunter.service.CacheService;
import com.ghosthunter.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status.
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HealthController {

    private final MonitoringService monitoringService;
    private final CacheService cacheService;

    /**
     * Application health check endpoint.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // Basic health status
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "ghost-hunter-backend");
        
        // Application metrics
        MonitoringService.ApplicationMetrics metrics = monitoringService.getApplicationMetrics();
        Map<String, Object> applicationMetrics = new HashMap<>();
        applicationMetrics.put("totalRequests", metrics.getTotalRequests());
        applicationMetrics.put("totalErrors", metrics.getTotalErrors());
        applicationMetrics.put("errorRate", metrics.getErrorRate());
        applicationMetrics.put("averageResponseTime", metrics.getAverageResponseTime());
        health.put("metrics", applicationMetrics);
        
        // Cache statistics
        CacheService.CacheStatistics cacheStats = cacheService.getCacheStatistics();
        Map<String, Object> cacheMetrics = new HashMap<>();
        cacheMetrics.put("telemetryStatsCount", cacheStats.getTelemetryStatsCount());
        cacheMetrics.put("heatMapCount", cacheStats.getHeatMapCount());
        cacheMetrics.put("userProfileCount", cacheStats.getUserProfileCount());
        health.put("cache", cacheMetrics);
        
        // System information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        health.put("system", systemInfo);
        
        log.debug("Health check requested");
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed metrics endpoint.
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Application metrics
        MonitoringService.ApplicationMetrics appMetrics = monitoringService.getApplicationMetrics();
        metrics.put("application", appMetrics);
        
        // Endpoint metrics
        MonitoringService.RequestMetrics[] endpointMetrics = monitoringService.getAllEndpointMetrics();
        metrics.put("endpoints", endpointMetrics);
        
        // Cache statistics
        CacheService.CacheStatistics cacheStats = cacheService.getCacheStatistics();
        metrics.put("cache", cacheStats);
        
        log.debug("Metrics requested");
        return ResponseEntity.ok(metrics);
    }

    /**
     * Cache status endpoint.
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> cacheStatus = new HashMap<>();
        
        // Cache statistics
        CacheService.CacheStatistics stats = cacheService.getCacheStatistics();
        cacheStatus.put("statistics", stats);
        
        // Cache health indicators
        Map<String, Boolean> health = new HashMap<>();
        health.put("telemetryStatsCached", stats.getTelemetryStatsCount() > 0);
        health.put("heatMapsCached", stats.getHeatMapCount() > 0);
        health.put("userProfilesCached", stats.getUserProfileCount() > 0);
        cacheStatus.put("health", health);
        
        log.debug("Cache status requested");
        return ResponseEntity.ok(cacheStatus);
    }

    /**
     * Reset all metrics.
     */
    @GetMapping("/reset-metrics")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        monitoringService.resetAllMetrics();
        cacheService.clearAllCaches();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All metrics and caches have been reset");
        response.put("status", "success");
        
        log.info("Metrics and caches reset");
        return ResponseEntity.ok(response);
    }
}