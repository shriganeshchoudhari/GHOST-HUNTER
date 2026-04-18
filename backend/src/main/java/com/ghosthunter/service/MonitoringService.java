package com.ghosthunter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for application monitoring and metrics collection.
 */
@Service
@Slf4j
public class MonitoringService {

    // Metrics counters
    private final ConcurrentHashMap<String, AtomicLong> requestCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> errorCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> responseTimeSum = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> responseTimeCount = new ConcurrentHashMap<>();

    /**
     * Record an API request.
     */
    public void recordRequest(String endpoint) {
        requestCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Record an API error.
     */
    public void recordError(String endpoint) {
        errorCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Record response time for an endpoint.
     */
    public void recordResponseTime(String endpoint, long responseTimeMs) {
        responseTimeSum.computeIfAbsent(endpoint, k -> new AtomicLong(0)).addAndGet(responseTimeMs);
        responseTimeCount.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
    }

    /**
     * Get request metrics for an endpoint.
     */
    public RequestMetrics getRequestMetrics(String endpoint) {
        RequestMetrics metrics = new RequestMetrics();
        metrics.setEndpoint(endpoint);
        metrics.setRequestCount(requestCounters.getOrDefault(endpoint, new AtomicLong(0)).get());
        metrics.setErrorCount(errorCounters.getOrDefault(endpoint, new AtomicLong(0)).get());
        
        AtomicLong sum = responseTimeSum.get(endpoint);
        AtomicLong count = responseTimeCount.get(endpoint);
        
        if (sum != null && count != null && count.get() > 0) {
            metrics.setAverageResponseTime(sum.get() / count.get());
        } else {
            metrics.setAverageResponseTime(0);
        }
        
        return metrics;
    }

    /**
     * Get overall application metrics.
     */
    public ApplicationMetrics getApplicationMetrics() {
        ApplicationMetrics metrics = new ApplicationMetrics();
        
        // Calculate total requests
        long totalRequests = requestCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        metrics.setTotalRequests(totalRequests);
        
        // Calculate total errors
        long totalErrors = errorCounters.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        metrics.setTotalErrors(totalErrors);
        
        // Calculate error rate
        if (totalRequests > 0) {
            metrics.setErrorRate((double) totalErrors / totalRequests * 100);
        } else {
            metrics.setErrorRate(0.0);
        }
        
        // Calculate average response time
        long totalResponseTime = responseTimeSum.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        long totalResponseCount = responseTimeCount.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
        
        if (totalResponseCount > 0) {
            metrics.setAverageResponseTime(totalResponseTime / totalResponseCount);
        } else {
            metrics.setAverageResponseTime(0);
        }
        
        return metrics;
    }

    /**
     * Reset metrics for an endpoint.
     */
    public void resetEndpointMetrics(String endpoint) {
        requestCounters.remove(endpoint);
        errorCounters.remove(endpoint);
        responseTimeSum.remove(endpoint);
        responseTimeCount.remove(endpoint);
        log.info("Reset metrics for endpoint: {}", endpoint);
    }

    /**
     * Reset all metrics.
     */
    public void resetAllMetrics() {
        requestCounters.clear();
        errorCounters.clear();
        responseTimeSum.clear();
        responseTimeCount.clear();
        log.info("Reset all metrics");
    }

    /**
     * Get all endpoint metrics.
     */
    public RequestMetrics[] getAllEndpointMetrics() {
        return requestCounters.keySet().stream()
                .map(this::getRequestMetrics)
                .toArray(RequestMetrics[]::new);
    }

    /**
     * Request metrics data class.
     */
    public static class RequestMetrics {
        private String endpoint;
        private long requestCount;
        private long errorCount;
        private long averageResponseTime;

        // Getters and setters
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        
        public long getRequestCount() { return requestCount; }
        public void setRequestCount(long requestCount) { this.requestCount = requestCount; }
        
        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }
        
        public long getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(long averageResponseTime) { this.averageResponseTime = averageResponseTime; }
    }

    /**
     * Application metrics data class.
     */
    public static class ApplicationMetrics {
        private long totalRequests;
        private long totalErrors;
        private double errorRate;
        private long averageResponseTime;

        // Getters and setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        
        public long getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(long averageResponseTime) { this.averageResponseTime = averageResponseTime; }
    }
}