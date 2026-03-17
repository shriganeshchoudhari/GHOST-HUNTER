package com.ghosthunter.service;

import com.ghosthunter.dto.TelemetryBatchRequest;
import com.ghosthunter.dto.TelemetryResponse;
import com.ghosthunter.dto.TelemetrySessionResponse;
import com.ghosthunter.dto.TelemetryStatistics;
import com.ghosthunter.dto.UserResponse;
import com.ghosthunter.exception.InvalidTelemetryDataException;
import com.ghosthunter.model.User;
import com.ghosthunter.model.WifiTelemetry;
import com.ghosthunter.repository.WifiTelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling WiFi telemetry data processing and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WifiTelemetryService {

    private final WifiTelemetryRepository telemetryRepository;
    private final UserService userService;

    /**
     * Submit a batch of telemetry measurements.
     *
     * @param userId User ID
     * @param batchRequest Batch telemetry request
     * @return List of saved telemetry responses
     */
    @Transactional
    public List<TelemetryResponse> submitTelemetryBatch(UUID userId, TelemetryBatchRequest batchRequest) {
        log.info("Submitting telemetry batch for user: {} with {} measurements", userId, batchRequest.getMeasurements().size());

        // Validate user exists
        UserResponse userResponse = userService.findById(userId);
        User user = new User();
        user.setId(userId);
        user.setUsername(userResponse.getUsername());
        user.setEmail(userResponse.getEmail());
        user.setCreatedAt(userResponse.getCreatedAt());

        // Validate batch request
        validateTelemetryBatch(batchRequest);

        // Process measurements
        List<WifiTelemetry> telemetryList = batchRequest.getMeasurements().stream()
                .map(measurement -> {
                    WifiTelemetry telemetry = new WifiTelemetry();
                    telemetry.setUser(user);
                    telemetry.setSessionId(batchRequest.getSessionId() != null ? UUID.fromString(batchRequest.getSessionId()) : null);
                    telemetry.setTimestamp(LocalDateTime.now());
                    telemetry.setRssi(measurement.getWifiRssi());
                    telemetry.setNoiseLevel(null);
                    telemetry.setLatencyMs(null);
                    telemetry.setPacketLossPercent(null);
                    telemetry.setThroughputMbps(null);
                    telemetry.setDeviceOrientation(null);
                    telemetry.setPositionApproximation(null);
                    telemetry.setLocationAccuracyMeters(null);
                    telemetry.setDeviceId(measurement.getDeviceModel());
                    return telemetry;
                })
                .collect(Collectors.toList());

        // Save all measurements
        List<WifiTelemetry> savedTelemetry = telemetryRepository.saveAll(telemetryList);

        log.info("Successfully saved {} telemetry measurements for user: {}", savedTelemetry.size(), userId);

        return savedTelemetry.stream()
                .map(TelemetryResponse::fromTelemetry)
                .collect(Collectors.toList());
    }

    /**
     * Get recent telemetry measurements for a user.
     *
     * @param userId User ID
     * @param limit Number of measurements to return
     * @return List of telemetry responses
     */
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getRecentTelemetry(UUID userId, int limit) {
        log.info("Getting recent telemetry for user: {} with limit: {}", userId, limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<WifiTelemetry> telemetryPage = telemetryRepository.findByUserIdOrderByTimestampDesc(userId, pageable);

        return telemetryPage.getContent().stream()
                .map(TelemetryResponse::fromTelemetry)
                .collect(Collectors.toList());
    }

    /**
     * Get telemetry measurements within a time range.
     *
     * @param userId User ID
     * @param startTime Start time
     * @param endTime End time
     * @param limit Maximum number of results
     * @return List of telemetry responses
     */
    @Transactional(readOnly = true)
    public List<TelemetryResponse> getTelemetryByTimeRange(UUID userId, LocalDateTime startTime, LocalDateTime endTime, int limit) {
        log.info("Getting telemetry for user: {} from {} to {} with limit: {}", userId, startTime, endTime, limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<WifiTelemetry> telemetryPage = telemetryRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                userId, startTime, endTime, pageable);

        return telemetryPage.getContent().stream()
                .map(TelemetryResponse::fromTelemetry)
                .collect(Collectors.toList());
    }

    /**
     * Get telemetry statistics for a user.
     *
     * @param userId User ID
     * @return Telemetry statistics
     */
    @Transactional(readOnly = true)
    public TelemetryStatistics getTelemetryStatistics(UUID userId) {
        log.info("Getting telemetry statistics for user: {}", userId);

        List<WifiTelemetry> telemetryData = telemetryRepository.findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, 1000)).getContent();

        if (telemetryData.isEmpty()) {
            return TelemetryStatistics.builder()
                    .totalMeasurements(0)
                    .avgRssi(0.0)
                    .maxRssi(0)
                    .minRssi(0)
                    .veryStrongSignals(0)
                    .strongSignals(0)
                    .mediumSignals(0)
                    .weakSignals(0)
                    .veryWeakSignals(0)
                    .build();
        }

        // Calculate statistics
        double avgRssi = telemetryData.stream()
                .mapToInt(WifiTelemetry::getRssi)
                .average()
                .orElse(0.0);

        int maxRssi = telemetryData.stream()
                .mapToInt(WifiTelemetry::getRssi)
                .max()
                .orElse(0);

        int minRssi = telemetryData.stream()
                .mapToInt(WifiTelemetry::getRssi)
                .min()
                .orElse(0);

        long totalMeasurements = telemetryData.size();
        
        // Calculate signal strength distribution
        long veryStrongSignals = telemetryData.stream()
                .filter(t -> t.getSignalStrengthCategory() == WifiTelemetry.SignalStrengthCategory.VERY_STRONG)
                .count();
        long strongSignals = telemetryData.stream()
                .filter(t -> t.getSignalStrengthCategory() == WifiTelemetry.SignalStrengthCategory.STRONG)
                .count();
        long mediumSignals = telemetryData.stream()
                .filter(t -> t.getSignalStrengthCategory() == WifiTelemetry.SignalStrengthCategory.MEDIUM)
                .count();
        long weakSignals = telemetryData.stream()
                .filter(t -> t.getSignalStrengthCategory() == WifiTelemetry.SignalStrengthCategory.WEAK)
                .count();
        long veryWeakSignals = telemetryData.stream()
                .filter(t -> t.getSignalStrengthCategory() == WifiTelemetry.SignalStrengthCategory.VERY_WEAK)
                .count();

        return TelemetryStatistics.builder()
                .totalMeasurements(totalMeasurements)
                .avgRssi(avgRssi)
                .maxRssi(maxRssi)
                .minRssi(minRssi)
                .veryStrongSignals(veryStrongSignals)
                .strongSignals(strongSignals)
                .mediumSignals(mediumSignals)
                .weakSignals(weakSignals)
                .veryWeakSignals(veryWeakSignals)
                .build();
    }

    /**
     * Get active telemetry sessions for a user.
     *
     * @param userId User ID
     * @return List of active session responses
     */
    @Transactional(readOnly = true)
    public List<TelemetrySessionResponse> getActiveSessions(UUID userId) {
        log.info("Getting active sessions for user: {}", userId);

        // Get sessions from the last 24 hours
        LocalDateTime cutoffTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        List<UUID> sessionIds = telemetryRepository.findActiveSessionsByUserId(userId, cutoffTime);

        return sessionIds.stream()
                .map(sessionId -> TelemetrySessionResponse.builder()
                        .sessionId(sessionId.toString())
                        .startTime(null)
                        .endTime(null)
                        .measurementCount(0L)
                        .avgRssi(0.0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Delete telemetry data older than specified days.
     *
     * @param userId User ID
     * @param daysToDelete Number of days of data to delete
     * @return Number of deleted records
     */
    @Transactional
    public long deleteOldTelemetryData(UUID userId, int daysToDelete) {
        log.info("Deleting telemetry data older than {} days for user: {}", daysToDelete, userId);

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToDelete);
        long deletedCount = telemetryRepository.deleteByUserIdAndTimestampBefore(userId, cutoffTime);

        log.info("Deleted {} telemetry records for user: {}", deletedCount, userId);
        return deletedCount;
    }

    /**
     * Validate telemetry batch request.
     */
    private void validateTelemetryBatch(TelemetryBatchRequest batchRequest) {
        if (batchRequest.getMeasurements() == null || batchRequest.getMeasurements().isEmpty()) {
            throw new InvalidTelemetryDataException("Telemetry batch cannot be empty");
        }

        if (batchRequest.getMeasurements().size() > 1000) {
            throw new InvalidTelemetryDataException("Telemetry batch size cannot exceed 1000 measurements");
        }

        // Validate individual measurements
        for (int i = 0; i < batchRequest.getMeasurements().size(); i++) {
            var measurement = batchRequest.getMeasurements().get(i);
            validateTelemetryMeasurement(measurement, i);
        }
    }

    /**
     * Validate individual telemetry measurement.
     */
    private void validateTelemetryMeasurement(TelemetryBatchRequest.Measurement measurement, int index) {
        if (measurement.getWifiRssi() < -100 || measurement.getWifiRssi() > 0) {
            throw new InvalidTelemetryDataException(
                    String.format("Invalid RSSI value at index %d: %d", index, measurement.getWifiRssi()));
        }
    }
}