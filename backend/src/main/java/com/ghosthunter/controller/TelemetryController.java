package com.ghosthunter.controller;

import com.ghosthunter.dto.TelemetryBatchRequest;
import com.ghosthunter.dto.TelemetryResponse;
import com.ghosthunter.dto.TelemetrySessionResponse;
import com.ghosthunter.dto.TelemetryStatistics;
import com.ghosthunter.service.WifiTelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for WiFi telemetry operations.
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TelemetryController {

    private final WifiTelemetryService telemetryService;

    /**
     * Submit a batch of telemetry measurements.
     *
     * @param principal Current authenticated user
     * @param batchRequest Batch telemetry request
     * @return List of saved telemetry responses
     */
    @PostMapping("/batch")
    public ResponseEntity<List<TelemetryResponse>> submitTelemetryBatch(
            Principal principal,
            @Valid @RequestBody TelemetryBatchRequest batchRequest) {
        
        log.info("Telemetry batch submission request from user: {} with {} measurements", 
                principal.getName(), batchRequest.getMeasurements().size());

        try {
            UUID userId = UUID.fromString(principal.getName());
            List<TelemetryResponse> responses = telemetryService.submitTelemetryBatch(userId, batchRequest);

            log.info("Successfully processed telemetry batch for user: {} with {} measurements", 
                    principal.getName(), responses.size());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error submitting telemetry batch for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get recent telemetry measurements for the current user.
     *
     * @param principal Current authenticated user
     * @param limit Number of measurements to return (default: 50)
     * @return List of telemetry responses
     */
    @GetMapping("/recent")
    public ResponseEntity<List<TelemetryResponse>> getRecentTelemetry(
            Principal principal,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Recent telemetry request from user: {} with limit: {}", principal.getName(), limit);

        try {
            UUID userId = UUID.fromString(principal.getName());
            List<TelemetryResponse> responses = telemetryService.getRecentTelemetry(userId, limit);

            log.info("Retrieved {} recent telemetry measurements for user: {}", 
                    responses.size(), principal.getName());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting recent telemetry for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get telemetry measurements within a time range.
     *
     * @param principal Current authenticated user
     * @param startTime Start time
     * @param endTime End time
     * @param limit Maximum number of results (default: 100)
     * @return List of telemetry responses
     */
    @GetMapping("/range")
    public ResponseEntity<List<TelemetryResponse>> getTelemetryByTimeRange(
            Principal principal,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(defaultValue = "100") int limit) {
        
        log.info("Telemetry range request from user: {} from {} to {} with limit: {}", 
                principal.getName(), startTime, endTime, limit);

        try {
            UUID userId = UUID.fromString(principal.getName());
            List<TelemetryResponse> responses = telemetryService.getTelemetryByTimeRange(
                    userId, startTime, endTime, limit);

            log.info("Retrieved {} telemetry measurements for user: {} in time range", 
                    responses.size(), principal.getName());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting telemetry range for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get telemetry statistics for the current user.
     *
     * @param principal Current authenticated user
     * @return Telemetry statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<TelemetryStatistics> getTelemetryStatistics(Principal principal) {
        
        log.info("Telemetry statistics request from user: {}", principal.getName());

        try {
            UUID userId = UUID.fromString(principal.getName());
            TelemetryStatistics statistics = telemetryService.getTelemetryStatistics(userId);

            log.info("Retrieved telemetry statistics for user: {}", principal.getName());
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting telemetry statistics for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get active telemetry sessions for the current user.
     *
     * @param principal Current authenticated user
     * @return List of active session responses
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<List<TelemetrySessionResponse>> getActiveSessions(Principal principal) {
        
        log.info("Active sessions request from user: {}", principal.getName());

        try {
            UUID userId = UUID.fromString(principal.getName());
            List<TelemetrySessionResponse> sessions = telemetryService.getActiveSessions(userId);

            log.info("Retrieved {} active sessions for user: {}", sessions.size(), principal.getName());
            
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting active sessions for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete old telemetry data for the current user.
     *
     * @param principal Current authenticated user
     * @param daysToDelete Number of days of data to delete
     * @return Number of deleted records
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Long> deleteOldTelemetryData(
            Principal principal,
            @RequestParam int daysToDelete) {
        
        log.info("Telemetry cleanup request from user: {} for {} days", principal.getName(), daysToDelete);

        try {
            UUID userId = UUID.fromString(principal.getName());
            long deletedCount = telemetryService.deleteOldTelemetryData(userId, daysToDelete);

            log.info("Deleted {} telemetry records for user: {}", deletedCount, principal.getName());
            
            return ResponseEntity.ok(deletedCount);
        } catch (Exception e) {
            log.error("Error deleting old telemetry data for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}