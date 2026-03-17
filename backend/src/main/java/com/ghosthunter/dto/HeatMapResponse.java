package com.ghosthunter.dto;

import com.ghosthunter.service.HeatMapService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for heat map responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatMapResponse {

    private String userId;
    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusMeters;
    private Double gridSizeMeters;
    private LocalDateTime timeRangeStart;
    private LocalDateTime timeRangeEnd;
    private HeatMapService.HeatMapGrid grid;
    private HeatMapStatistics statistics;
    private LocalDateTime generatedAt;

    public static HeatMapResponse empty(HeatMapRequest request) {
        return HeatMapResponse.builder()
                .userId(null)
                .centerLatitude(request.getCenterLatitude())
                .centerLongitude(request.getCenterLongitude())
                .radiusMeters(request.getRadiusMeters())
                .gridSizeMeters(request.getGridSizeMeters())
                .timeRangeStart(request.getTimeRangeStart())
                .timeRangeEnd(request.getTimeRangeEnd())
                .grid(new HeatMapService.HeatMapGrid())
                .statistics(HeatMapStatistics.empty())
                .generatedAt(LocalDateTime.now())
                .build();
    }
}