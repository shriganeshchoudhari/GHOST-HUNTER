package com.ghosthunter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for telemetry session responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetrySessionResponse {

    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long measurementCount;
    private Double avgRssi;
}