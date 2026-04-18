package com.ghosthunter.dto;

import com.ghosthunter.model.WifiTelemetry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for telemetry responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryResponse {

    private String id;
    private String userId;
    private String sessionId;
    private LocalDateTime timestamp;
    
    // Location data
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double accuracy;

    // WiFi data
    private String wifiSsid;
    private String wifiBssid;
    private Integer wifiFrequency;
    private Integer wifiChannel;
    private Integer wifiBandwidth;
    private Integer wifiLinkSpeed;
    private Integer wifiRssi;
    private String wifiSecurity;

    // Device information
    private String deviceModel;
    private String deviceManufacturer;
    private String deviceOs;
    private String deviceOsVersion;
    private String appVersion;

    // Calculated fields
    private String signalStrengthCategory;
    private Double signalQuality;
    private String connectionStatus;
    private String networkType;
    private String optimalPositioning;

    // Measurement metadata
    private String measurementType;
    private String measurementMode;
    private String measurementAccuracy;
    private String measurementConfidence;
    private String measurementEnvironment;
    private String measurementPurpose;
    private String measurementTags;
    private String measurementMetadata;

    /**
     * Convert WifiTelemetry entity to TelemetryResponse DTO.
     */
    public static TelemetryResponse fromTelemetry(WifiTelemetry telemetry) {
        return TelemetryResponse.builder()
                .id(telemetry.getId().toString())
                .userId(telemetry.getUser() != null ? telemetry.getUser().getId().toString() : null)
                .sessionId(telemetry.getSessionId() != null ? telemetry.getSessionId().toString() : null)
                .timestamp(telemetry.getTimestamp())
                .latitude(telemetry.getLatitude() != null ? telemetry.getLatitude().doubleValue() : null)
                .longitude(telemetry.getLongitude() != null ? telemetry.getLongitude().doubleValue() : null)
                .altitude(telemetry.getAltitude() != null ? telemetry.getAltitude().doubleValue() : null)
                .accuracy(telemetry.getAccuracy() != null ? telemetry.getAccuracy().doubleValue() : null)
                .wifiSsid(telemetry.getWifiSsid())
                .wifiBssid(telemetry.getWifiBssid())
                .wifiFrequency(telemetry.getWifiFrequency())
                .wifiChannel(telemetry.getWifiChannel())
                .wifiBandwidth(telemetry.getWifiBandwidth())
                .wifiLinkSpeed(telemetry.getWifiLinkSpeed())
                .wifiRssi(telemetry.getWifiRssi())
                .wifiSecurity(telemetry.getWifiSecurity())
                .deviceModel(telemetry.getDeviceModel())
                .deviceManufacturer(telemetry.getDeviceManufacturer())
                .deviceOs(telemetry.getDeviceOs())
                .deviceOsVersion(telemetry.getDeviceOsVersion())
                .appVersion(telemetry.getAppVersion())
                .signalStrengthCategory(telemetry.getSignalStrengthCategory() != null ? telemetry.getSignalStrengthCategory().name() : null)
                .signalQuality(telemetry.getSignalQuality() != null ? Double.parseDouble(telemetry.getSignalQuality()) : null)
                .connectionStatus(telemetry.getConnectionStatus())
                .networkType(telemetry.getNetworkType())
                .optimalPositioning(telemetry.getOptimalPositioning())
                .measurementType(telemetry.getMeasurementType())
                .measurementMode(telemetry.getMeasurementMode())
                .measurementAccuracy(telemetry.getMeasurementAccuracy())
                .measurementConfidence(telemetry.getMeasurementConfidence())
                .measurementEnvironment(telemetry.getMeasurementEnvironment())
                .measurementPurpose(telemetry.getMeasurementPurpose())
                .measurementTags(telemetry.getMeasurementTags())
                .measurementMetadata(telemetry.getMeasurementMetadata())
                .build();
    }
}