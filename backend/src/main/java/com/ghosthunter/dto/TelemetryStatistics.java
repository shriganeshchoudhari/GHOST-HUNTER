package com.ghosthunter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for telemetry statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryStatistics {

    private long totalMeasurements;
    private double avgRssi;
    private int maxRssi;
    private int minRssi;
    private long veryStrongSignals;
    private long strongSignals;
    private long mediumSignals;
    private long weakSignals;
    private long veryWeakSignals;
}