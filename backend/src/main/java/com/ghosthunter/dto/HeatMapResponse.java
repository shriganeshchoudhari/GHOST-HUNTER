package com.ghosthunter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HeatMapGrid grid;
    private HeatMapStatistics statistics;
    private LocalDateTime generatedAt;

    public static HeatMapResponse empty(HeatMapRequest request) {
        return HeatMapResponse.builder()
                .centerLatitude(request.getCenterLatitude())
                .centerLongitude(request.getCenterLongitude())
                .radiusMeters(request.getRadiusMeters())
                .gridSizeMeters(request.getGridSizeMeters())
                .timeRangeStart(request.getTimeRangeStart())
                .timeRangeEnd(request.getTimeRangeEnd())
                .grid(HeatMapGrid.builder()
                        .centerLatitude(request.getCenterLatitude())
                        .centerLongitude(request.getCenterLongitude())
                        .gridSizeMeters(request.getGridSizeMeters())
                        .radiusMeters(request.getRadiusMeters())
                        .cells(new ArrayList<>())
                        .build())
                .statistics(HeatMapStatistics.empty())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Heat map grid data class.
     */
    @lombok.Builder
    public static class HeatMapGrid {
        private double centerLatitude;
        private double centerLongitude;
        private double gridSizeMeters;
        private double radiusMeters;
        private List<HeatMapCell> cells;

        // Getters and setters
        public double getCenterLatitude() { return centerLatitude; }
        public void setCenterLatitude(double centerLatitude) { this.centerLatitude = centerLatitude; }
        
        public double getCenterLongitude() { return centerLongitude; }
        public void setCenterLongitude(double centerLongitude) { this.centerLongitude = centerLongitude; }
        
        public double getGridSizeMeters() { return gridSizeMeters; }
        public void setGridSizeMeters(double gridSizeMeters) { this.gridSizeMeters = gridSizeMeters; }
        
        public double getRadiusMeters() { return radiusMeters; }
        public void setRadiusMeters(double radiusMeters) { this.radiusMeters = radiusMeters; }
        
        public List<HeatMapCell> getCells() { return cells; }
        public void setCells(List<HeatMapCell> cells) { this.cells = cells; }
    }

    /**
     * Heat map cell data class.
     */
    @lombok.Builder
    public static class HeatMapCell {
        private double latitude;
        private double longitude;
        private long measurementCount;
        private double averageRssi;
        private String signalStrengthCategory;

        // Getters and setters
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public long getMeasurementCount() { return measurementCount; }
        public void setMeasurementCount(long measurementCount) { this.measurementCount = measurementCount; }
        
        public double getAverageRssi() { return averageRssi; }
        public void setAverageRssi(double averageRssi) { this.averageRssi = averageRssi; }
        
        public String getSignalStrengthCategory() { return signalStrengthCategory; }
        public void setSignalStrengthCategory(String signalStrengthCategory) { this.signalStrengthCategory = signalStrengthCategory; }
    }

    /**
     * Position recommendation data class.
     */
    @lombok.Builder
    public static class PositionRecommendation {
        private String signalCategory;
        private double latitude;
        private double longitude;
        private double averageRssi;
        private long measurementCount;

        // Getters and setters
        public String getSignalCategory() { return signalCategory; }
        public void setSignalCategory(String signalCategory) { this.signalCategory = signalCategory; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public double getAverageRssi() { return averageRssi; }
        public void setAverageRssi(double averageRssi) { this.averageRssi = averageRssi; }
        
        public long getMeasurementCount() { return measurementCount; }
        public void setMeasurementCount(long measurementCount) { this.measurementCount = measurementCount; }
    }

    /**
     * Positioning recommendations data class.
     */
    @lombok.Builder
    public static class PositioningRecommendations {
        private Map<String, PositionRecommendation> recommendations;
        private PositionRecommendation bestPosition;
        private List<String> improvementSuggestions;

        public static PositioningRecommendations empty() {
            return PositioningRecommendations.builder()
                    .recommendations(new HashMap<>())
                    .bestPosition(null)
                    .improvementSuggestions(new ArrayList<>())
                    .build();
        }

        // Getters and setters
        public Map<String, PositionRecommendation> getRecommendations() { return recommendations; }
        public void setRecommendations(Map<String, PositionRecommendation> recommendations) { this.recommendations = recommendations; }
        
        public PositionRecommendation getBestPosition() { return bestPosition; }
        public void setBestPosition(PositionRecommendation bestPosition) { this.bestPosition = bestPosition; }
        
        public List<String> getImprovementSuggestions() { return improvementSuggestions; }
        public void setImprovementSuggestions(List<String> improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }
    }
}
