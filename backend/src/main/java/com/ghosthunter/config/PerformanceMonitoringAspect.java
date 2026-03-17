package com.ghosthunter.config;

import com.ghosthunter.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for monitoring API performance and collecting metrics.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceMonitoringAspect {

    private final MonitoringService monitoringService;

    /**
     * Monitor API endpoint performance.
     */
    @Around("execution(* com.ghosthunter.controller.*.*(..))")
    public Object monitorApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String endpoint = className + "." + methodName;
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            // Record request
            monitoringService.recordRequest(endpoint);
            
            // Execute the method
            Object result = joinPoint.proceed();
            success = true;
            
            return result;
        } catch (Exception e) {
            // Record error
            monitoringService.recordError(endpoint);
            throw e;
        } finally {
            // Record response time
            long responseTime = System.currentTimeMillis() - startTime;
            monitoringService.recordResponseTime(endpoint, responseTime);
            
            // Log performance metrics
            if (responseTime > 1000) { // Log slow requests
                log.warn("Slow API response: {} took {}ms", endpoint, responseTime);
            } else if (log.isDebugEnabled()) {
                log.debug("API response: {} took {}ms", endpoint, responseTime);
            }
        }
    }

    /**
     * Monitor service method performance.
     */
    @Around("execution(* com.ghosthunter.service.*.*(..)) && !execution(* com.ghosthunter.service.MonitoringService.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String operation = className + "." + methodName;
        
        long startTime = System.currentTimeMillis();
        boolean success = false;
        
        try {
            // Execute the method
            Object result = joinPoint.proceed();
            success = true;
            
            return result;
        } catch (Exception e) {
            log.error("Service method failed: {}", operation, e);
            throw e;
        } finally {
            // Log performance metrics
            long responseTime = System.currentTimeMillis() - startTime;
            if (responseTime > 500) { // Log slow service operations
                log.warn("Slow service operation: {} took {}ms", operation, responseTime);
            } else if (log.isDebugEnabled()) {
                log.debug("Service operation: {} took {}ms", operation, responseTime);
            }
        }
    }
}