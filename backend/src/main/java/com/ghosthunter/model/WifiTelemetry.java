package com.ghosthunter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WiFi Telemetry entity representing a single WiFi signal measurement.
 */
@Entity
@Table(name = "wifi_telemetry", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp DESC"),
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_rssi", columnList = "rssi")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WifiTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rssi;

    @Column
    private Integer noiseLevel;

    @Column
    private Integer latencyMs;

    @Column(precision = 5, scale = 2)
    private BigDecimal packetLossPercent;

    @Column(precision = 10, scale = 2)
    private BigDecimal throughputMbps;

    @Column(columnDefinition = "jsonb")
    private String deviceOrientation;

    @Column(columnDefinition = "jsonb")
    private String positionApproximation;

    @Column(precision = 10, scale = 2)
    private BigDecimal locationAccuracyMeters;

    @Column(length = 255)
    private String deviceId;

    @Column
    private UUID sessionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Get signal strength category based on RSSI value.
     * 
     * @return Signal strength category
     */
    public SignalStrengthCategory getSignalStrengthCategory() {
        if (rssi == null) {
            return SignalStrengthCategory.MEDIUM; // Default to medium if null
        }
        if (rssi < -85) {
            return SignalStrengthCategory.VERY_WEAK;
        } else if (rssi < -70) {
            return SignalStrengthCategory.WEAK;
        } else if (rssi < -60) {
            return SignalStrengthCategory.MEDIUM;
        } else if (rssi < -50) {
            return SignalStrengthCategory.STRONG;
        } else {
            return SignalStrengthCategory.VERY_STRONG;
        }
    }

    /**
     * Signal strength category enumeration.
     */
    public enum SignalStrengthCategory {
        VERY_WEAK,
        WEAK,
        MEDIUM,
        STRONG,
        VERY_STRONG
    }
}
