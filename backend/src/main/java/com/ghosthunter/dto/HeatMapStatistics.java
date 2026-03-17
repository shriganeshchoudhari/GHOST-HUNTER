package com.ghosthunter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for heat map statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatMapStatistics {

    private long totalMeasurements;
    private long totalGridCells;
    private double averageRssi;
    private long excellentAreas;
    private long goodAreas;
    private long fairAreas;
    private long poorAreas;
    private long veryPoorAreas;
    private Double optimalPositionLatitude;
    private Double optimalPositionLongitude;
    private double optimalPositionRssi;

    public static HeatMapStatistics empty() {
        HeatMapStatistics stats = new HeatMapStatistics();
        stats.totalMeasurements = 0;
        stats.totalGridCells = 0;
        stats.averageRssi = 0.0;
        stats.excellentAreas = 0;
        stats.goodAreas = 0;
        stats.fairAreas = 0;
        stats.poorAreas = 0;
        stats.veryPoorAreas = 0;
        stats.optimalPositionLatitude = null;
        stats.optimalPositionLongitude = null;
        stats.optimalPositionRssi = 0.0;
        return stats;
    }
}