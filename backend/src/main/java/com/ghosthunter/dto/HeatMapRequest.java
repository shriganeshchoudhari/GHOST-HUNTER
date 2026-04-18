package com.ghosthunter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for heat map generation requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatMapRequest {

    private Double centerLatitude;
    private Double centerLongitude;
    private Double radiusMeters;
    private Double gridSizeMeters;
    private LocalDateTime timeRangeStart;
    private LocalDateTime timeRangeEnd;
}