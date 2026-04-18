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

    // Additional WiFi measurement fields
    @Column(length = 255)
    private String wifiSsid;

    @Column(length = 17)
    private String wifiBssid;

    @Column
    private Integer wifiFrequency;

    @Column
    private Integer wifiChannel;

    @Column
    private Integer wifiBandwidth;

    @Column
    private Integer wifiLinkSpeed;

    @Column
    private Integer wifiRssi;

    @Column(length = 50)
    private String wifiSecurity;

    // Device information fields
    @Column(length = 100)
    private String deviceModel;

    @Column(length = 100)
    private String deviceManufacturer;

    @Column(length = 50)
    private String deviceOs;

    @Column(length = 50)
    private String deviceOsVersion;

    @Column(length = 20)
    private String appVersion;

    // Signal quality and connection status
    @Column(length = 50)
    private String signalQuality;

    @Column(length = 50)
    private String connectionStatus;

    @Column(length = 50)
    private String networkType;

    @Column(length = 100)
    private String optimalPositioning;

    // Measurement metadata
    @Column(length = 50)
    private String measurementType;

    @Column(length = 50)
    private String measurementMode;

    @Column(length = 50)
    private String measurementAccuracy;

    @Column(length = 50)
    private String measurementConfidence;

    @Column(length = 100)
    private String measurementEnvironment;

    @Column(length = 100)
    private String measurementPurpose;

    @Column(columnDefinition = "jsonb")
    private String measurementTags;

    @Column(columnDefinition = "jsonb")
    private String measurementMetadata;

    // Spatial data fields
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal altitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal accuracy;

    // Getters for spatial data
    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public BigDecimal getAltitude() {
        return altitude;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    // Getters for WiFi information
    public String getWifiSsid() {
        return wifiSsid;
    }

    public String getWifiBssid() {
        return wifiBssid;
    }

    public Integer getWifiFrequency() {
        return wifiFrequency;
    }

    public Integer getWifiChannel() {
        return wifiChannel;
    }

    public Integer getWifiBandwidth() {
        return wifiBandwidth;
    }

    public Integer getWifiLinkSpeed() {
        return wifiLinkSpeed;
    }

    public Integer getWifiRssi() {
        return wifiRssi;
    }

    public String getWifiSecurity() {
        return wifiSecurity;
    }

    // Getters for device information
    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public String getDeviceOsVersion() {
        return deviceOsVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    // Getters for signal quality and connection status
    public String getSignalQuality() {
        return signalQuality;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getOptimalPositioning() {
        return optimalPositioning;
    }

    // Getters for measurement metadata
    public String getMeasurementType() {
        return measurementType;
    }

    public String getMeasurementMode() {
        return measurementMode;
    }

    public String getMeasurementAccuracy() {
        return measurementAccuracy;
    }

    public String getMeasurementConfidence() {
        return measurementConfidence;
    }

    public String getMeasurementEnvironment() {
        return measurementEnvironment;
    }

    public String getMeasurementPurpose() {
        return measurementPurpose;
    }

    public String getMeasurementTags() {
        return measurementTags;
    }

    public String getMeasurementMetadata() {
        return measurementMetadata;
    }

    /**
     * Get signal strength category based on RSSI value.
     * 
     * @return Signal strength category
     */
    public SignalStrengthCategory getSignalStrengthCategory() {
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

    // Helper methods for BigDecimal/Double conversion
    public Double getLatitudeAsDouble() {
        return latitude != null ? latitude.doubleValue() : null;
    }

    public Double getLongitudeAsDouble() {
        return longitude != null ? longitude.doubleValue() : null;
    }
}
