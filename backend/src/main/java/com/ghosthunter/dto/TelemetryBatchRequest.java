package com.ghosthunter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for telemetry batch requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryBatchRequest {

    @NotNull(message = "Session ID is required")
    private String sessionId;

    @NotEmpty(message = "Measurements cannot be empty")
    @Size(max = 1000, message = "Batch size cannot exceed 1000 measurements")
    private List<@Valid Measurement> measurements;

    /**
     * Individual telemetry measurement.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Measurement {
        
        // Location data
        @NotNull(message = "Latitude is required")
        private Double latitude;
        
        @NotNull(message = "Longitude is required")
        private Double longitude;
        
        private Double altitude;
        private Double accuracy;

        // WiFi data
        private String wifiSsid;
        private String wifiBssid;
        
        @NotNull(message = "WiFi frequency is required")
        private Integer wifiFrequency;
        
        private Integer wifiChannel;
        private Integer wifiBandwidth;
        private Integer wifiLinkSpeed;
        
        @NotNull(message = "WiFi RSSI is required")
        private Integer wifiRssi;
        
        private String wifiSecurity;

        // Device information
        private String deviceModel;
        private String deviceManufacturer;
        private String deviceOs;
        private String deviceOsVersion;
        private String appVersion;

        // Measurement metadata
        private String measurementType;
        private String measurementMode;
        private String measurementAccuracy;
        private String measurementConfidence;
        private String measurementEnvironment;
        private String measurementPurpose;
        private String measurementTags;
        private String measurementMetadata;
    }
}