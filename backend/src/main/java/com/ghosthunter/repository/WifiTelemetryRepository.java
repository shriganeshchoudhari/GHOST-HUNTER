package com.ghosthunter.repository;

import com.ghosthunter.model.WifiTelemetry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for WifiTelemetry entity.
 */
@Repository
public interface WifiTelemetryRepository extends JpaRepository<WifiTelemetry, UUID> {

    /**
     * Find recent telemetry measurements for a user.
     * 
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of telemetry measurements
     */
    Page<WifiTelemetry> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    /**
     * Find telemetry measurements for a specific session.
     * 
     * @param sessionId the session ID
     * @return list of telemetry measurements
     */
    List<WifiTelemetry> findBySessionIdOrderByTimestampAsc(UUID sessionId);

    /**
     * Find telemetry measurements within a time range.
     * 
     * @param userId the user ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of telemetry measurements
     */
    @Query("SELECT wt FROM WifiTelemetry wt WHERE wt.user.id = :userId " +
           "AND wt.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY wt.timestamp ASC")
    List<WifiTelemetry> findByUserAndTimeRange(
        @Param("userId") UUID userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Get average RSSI for a user within a time range.
     * 
     * @param userId the user ID
     * @param startTime the start time
     * @param endTime the end time
     * @return average RSSI value
     */
    @Query("SELECT AVG(wt.rssi) FROM WifiTelemetry wt WHERE wt.user.id = :userId " +
           "AND wt.timestamp BETWEEN :startTime AND :endTime")
    Double getAverageRssi(
        @Param("userId") UUID userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Get measurement count for a user within a time range.
     * 
     * @param userId the user ID
     * @param startTime the start time
     * @param endTime the end time
     * @return count of measurements
     */
    @Query("SELECT COUNT(wt) FROM WifiTelemetry wt WHERE wt.user.id = :userId " +
           "AND wt.timestamp BETWEEN :startTime AND :endTime")
    long countByUserAndTimeRange(
        @Param("userId") UUID userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
