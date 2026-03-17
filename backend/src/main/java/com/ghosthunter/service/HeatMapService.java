package com.ghosthunter.service;

import com.ghosthunter.dto.HeatMapRequest;
import com.ghosthunter.dto.HeatMapResponse;
import com.ghosthunter.dto.HeatMapStatistics;
import com.ghosthunter.exception.InvalidHeatMapDataException;
import com.ghosthunter.model.User;
import com.ghosthunter.model.WifiTelemetry;
import com.ghosthunter.repository.WifiTelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating and managing WiFi heat maps.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HeatMapService {

    private final WifiTelemetryRepository telemetryRepository;
    private final UserService userService;

    /**
     * Generate a heat map for a user within a specified area and time range.
     *
     * @param userId User ID
     * @param request Heat map generation request
     * @return Generated heat map response
     */
    @Transactional(readOnly = true)
    public HeatMapResponse generateHeatMap(UUID userId, HeatMapRequest request) {
        log.info("Generating heat map for user: {} with request: {}", userId, request);

        // Validate user exists
        User user = userService.findById(userId);

        // Validate request parameters
        validateHeatMapRequest(request);

        // Get telemetry data for the specified area and time range
        List<WifiTelemetry> telemetryData = getTelemetryData(userId, request);

        if (telemetryData.isEmpty()) {
            log.warn("No telemetry data found for heat map generation for user: {}", userId);
            return HeatMapResponse.empty(request);
        }

        // Generate heat map grid
        HeatMapGrid grid = generateHeatMapGrid(telemetryData, request);

        // Calculate statistics
        HeatMapStatistics statistics = calculateHeatMapStatistics(grid);

        log.info("Successfully generated heat map for user: {} with {} grid cells", userId, grid.getCells().size());

        return HeatMapResponse.builder()
                .userId(userId.toString())
                .centerLatitude(request.getCenterLatitude())
                .centerLongitude(request.getCenterLongitude())
                .radiusMeters(request.getRadiusMeters())
                .gridSizeMeters(request.getGridSizeMeters())
                .timeRangeStart(request.getTimeRangeStart())
                .timeRangeEnd(request.getTimeRangeEnd())
                .grid(grid)
                .statistics(statistics)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Get heat map statistics for a user.
     *
     * @param userId User ID
     * @param request Heat map request
     * @return Heat map statistics
     */
    @Transactional(readOnly = true)
    public HeatMapStatistics getHeatMapStatistics(UUID userId, HeatMapRequest request) {
        log.info("Getting heat map statistics for user: {}", userId);

        // Validate user exists
        userService.findById(userId);

        // Validate request parameters
        validateHeatMapRequest(request);

        // Get telemetry data
        List<WifiTelemetry> telemetryData = getTelemetryData(userId, request);

        if (telemetryData.isEmpty()) {
            return HeatMapStatistics.empty();
        }

        // Generate heat map grid
        HeatMapGrid grid = generateHeatMapGrid(telemetryData, request);

        return calculateHeatMapStatistics(grid);
    }

    /**
     * Get user's saved heat maps.
     *
     * @param userId User ID
     * @param limit Number of heat maps to return
     * @return List of saved heat map responses
     */
    @Transactional(readOnly = true)
    public List<HeatMapResponse> getSavedHeatMaps(UUID userId, int limit) {
        log.info("Getting saved heat maps for user: {} with limit: {}", userId, limit);

        // This would typically query a saved_heat_maps table
        // For now, return empty list as we don't have persistence for saved heat maps yet
        return new ArrayList<>();
    }

    /**
     * Save a heat map for a user.
     *
     * @param userId User ID
     * @param heatMapResponse Heat map to save
     * @return Saved heat map response
     */
    @Transactional
    public HeatMapResponse saveHeatMap(UUID userId, HeatMapResponse heatMapResponse) {
        log.info("Saving heat map for user: {}", userId);

        // This would typically save to a saved_heat_maps table
        // For now, just return the heat map as-is
        return heatMapResponse;
    }

    /**
     * Get optimal WiFi positioning recommendations.
     *
     * @param userId User ID
     * @param request Heat map request
     * @return Positioning recommendations
     */
    @Transactional(readOnly = true)
    public PositioningRecommendations getOptimalPositioningRecommendations(UUID userId, HeatMapRequest request) {
        log.info("Getting positioning recommendations for user: {}", userId);

        // Validate user exists
        userService.findById(userId);

        // Validate request parameters
        validateHeatMapRequest(request);

        // Get telemetry data
        List<WifiTelemetry> telemetryData = getTelemetryData(userId, request);

        if (telemetryData.isEmpty()) {
            return PositioningRecommendations.empty();
        }

        // Analyze signal patterns
        return analyzeSignalPatterns(telemetryData, request);
    }

    /**
     * Get telemetry data for heat map generation.
     */
    private List<WifiTelemetry> getTelemetryData(UUID userId, HeatMapRequest request) {
        Pageable pageable = PageRequest.of(0, 10000); // Limit to prevent memory issues
        
        Page<WifiTelemetry> telemetryPage;
        
        if (request.getTimeRangeStart() != null && request.getTimeRangeEnd() != null) {
            telemetryPage = telemetryRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(
                    userId, request.getTimeRangeStart(), request.getTimeRangeEnd(), pageable);
        } else {
            telemetryPage = telemetryRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        }

        return filterByGeographicArea(telemetryPage.getContent(), request);
    }

    /**
     * Filter telemetry data by geographic area.
     */
    private List<WifiTelemetry> filterByGeographicArea(List<WifiTelemetry> telemetryData, HeatMapRequest request) {
        if (request.getCenterLatitude() == null || request.getCenterLongitude() == null || request.getRadiusMeters() == null) {
            return telemetryData;
        }

        return telemetryData.stream()
                .filter(telemetry -> {
                    if (telemetry.getLatitude() == null || telemetry.getLongitude() == null) {
                        return false;
                    }
                    
                    double distance = calculateDistance(
                            request.getCenterLatitude(), request.getCenterLongitude(),
                            telemetry.getLatitude(), telemetry.getLongitude()
                    );
                    
                    return distance <= request.getRadiusMeters();
                })
                .collect(Collectors.toList());
    }

    /**
     * Generate heat map grid.
     */
    private HeatMapGrid generateHeatMapGrid(List<WifiTelemetry> telemetryData, HeatMapRequest request) {
        double gridSize = request.getGridSizeMeters();
        double centerLat = request.getCenterLatitude();
        double centerLon = request.getCenterLongitude();
        double radius = request.getRadiusMeters();

        // Calculate grid boundaries
        double latRange = radius / 111000.0; // Approximate meters to degrees
        double lonRange = radius / (111000.0 * Math.cos(Math.toRadians(centerLat)));

        double minLat = centerLat - latRange;
        double maxLat = centerLat + latRange;
        double minLon = centerLon - lonRange;
        double maxLon = centerLon + lonRange;

        // Generate grid cells
        List<HeatMapCell> cells = new ArrayList<>();
        
        for (double lat = minLat; lat <= maxLat; lat += gridSize / 111000.0) {
            for (double lon = minLon; lon <= maxLon; lon += gridSize / (111000.0 * Math.cos(Math.toRadians(lat)))) {
                if (calculateDistance(centerLat, centerLon, lat, lon) <= radius) {
                    HeatMapCell cell = createHeatMapCell(telemetryData, lat, lon, gridSize);
                    if (cell.getMeasurementCount() > 0) {
                        cells.add(cell);
                    }
                }
            }
        }

        return HeatMapGrid.builder()
                .centerLatitude(centerLat)
                .centerLongitude(centerLon)
                .gridSizeMeters(gridSize)
                .radiusMeters(radius)
                .cells(cells)
                .build();
    }

    /**
     * Create a heat map cell.
     */
    private HeatMapCell createHeatMapCell(List<WifiTelemetry> telemetryData, double latitude, double longitude, double gridSize) {
        double gridSizeHalf = gridSize / 2.0;
        
        List<WifiTelemetry> cellData = telemetryData.stream()
                .filter(t -> t.getLatitude() != null && t.getLongitude() != null)
                .filter(t -> Math.abs(t.getLatitude() - latitude) <= gridSizeHalf / 111000.0)
                .filter(t -> Math.abs(t.getLongitude() - longitude) <= gridSizeHalf / (111000.0 * Math.cos(Math.toRadians(latitude))))
                .collect(Collectors.toList());

        if (cellData.isEmpty()) {
            return HeatMapCell.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .measurementCount(0)
                    .averageRssi(0.0)
                    .signalStrengthCategory("NO_DATA")
                    .build();
        }

        double avgRssi = cellData.stream()
                .mapToInt(WifiTelemetry::getRssi)
                .average()
                .orElse(0.0);

        String signalCategory = calculateSignalCategory(avgRssi);

        return HeatMapCell.builder()
                .latitude(latitude)
                .longitude(longitude)
                .measurementCount(cellData.size())
                .averageRssi(avgRssi)
                .signalStrengthCategory(signalCategory)
                .build();
    }

    /**
     * Calculate signal strength category.
     */
    private String calculateSignalCategory(double avgRssi) {
        if (avgRssi >= -50) return "EXCELLENT";
        if (avgRssi >= -60) return "GOOD";
        if (avgRssi >= -70) return "FAIR";
        if (avgRssi >= -80) return "POOR";
        return "VERY_POOR";
    }

    /**
     * Calculate heat map statistics.
     */
    private HeatMapStatistics calculateHeatMapStatistics(HeatMapGrid grid) {
        List<HeatMapCell> cells = grid.getCells();
        
        if (cells.isEmpty()) {
            return HeatMapStatistics.empty();
        }

        long totalMeasurements = cells.stream()
                .mapToLong(HeatMapCell::getMeasurementCount)
                .sum();

        double avgRssi = cells.stream()
                .filter(cell -> cell.getMeasurementCount() > 0)
                .mapToDouble(HeatMapCell::getAverageRssi)
                .average()
                .orElse(0.0);

        long excellentAreas = cells.stream()
                .filter(cell -> "EXCELLENT".equals(cell.getSignalStrengthCategory()))
                .count();
        long goodAreas = cells.stream()
                .filter(cell -> "GOOD".equals(cell.getSignalStrengthCategory()))
                .count();
        long fairAreas = cells.stream()
                .filter(cell -> "FAIR".equals(cell.getSignalStrengthCategory()))
                .count();
        long poorAreas = cells.stream()
                .filter(cell -> "POOR".equals(cell.getSignalStrengthCategory()))
                .count();
        long veryPoorAreas = cells.stream()
                .filter(cell -> "VERY_POOR".equals(cell.getSignalStrengthCategory()))
                .count();

        // Find optimal positioning
        Optional<HeatMapCell> bestCell = cells.stream()
                .filter(cell -> cell.getMeasurementCount() > 0)
                .max(Comparator.comparingDouble(HeatMapCell::getAverageRssi));

        HeatMapStatistics statistics = new HeatMapStatistics();
        statistics.setTotalMeasurements(totalMeasurements);
        statistics.setTotalGridCells(cells.size());
        statistics.setAverageRssi(avgRssi);
        statistics.setExcellentAreas(excellentAreas);
        statistics.setGoodAreas(goodAreas);
        statistics.setFairAreas(fairAreas);
        statistics.setPoorAreas(poorAreas);
        statistics.setVeryPoorAreas(veryPoorAreas);
        statistics.setOptimalPositionLatitude(bestCell.map(HeatMapCell::getLatitude).orElse(null));
        statistics.setOptimalPositionLongitude(bestCell.map(HeatMapCell::getLongitude).orElse(null));
        statistics.setOptimalPositionRssi(bestCell.map(HeatMapCell::getAverageRssi).orElse(0.0));
        return statistics;
    }

    /**
     * Analyze signal patterns for positioning recommendations.
     */
    private PositioningRecommendations analyzeSignalPatterns(List<WifiTelemetry> telemetryData, HeatMapRequest request) {
        // Group by signal strength
        Map<String, List<WifiTelemetry>> signalGroups = telemetryData.stream()
                .collect(Collectors.groupingBy(t -> {
                    int rssi = t.getRssi();
                    if (rssi >= -50) return "EXCELLENT";
                    if (rssi >= -60) return "GOOD";
                    if (rssi >= -70) return "FAIR";
                    if (rssi >= -80) return "POOR";
                    return "VERY_POOR";
                }));

        // Calculate average positions for each signal group
        Map<String, PositionRecommendation> recommendations = new HashMap<>();
        
        for (Map.Entry<String, List<WifiTelemetry>> entry : signalGroups.entrySet()) {
            String signalCategory = entry.getKey();
            List<WifiTelemetry> groupData = entry.getValue();
            
            if (groupData.size() > 0) {
                double avgLat = groupData.stream()
                        .mapToDouble(t -> t.getLatitude() != null ? t.getLatitude() : 0.0)
                        .average()
                        .orElse(0.0);
                
                double avgLon = groupData.stream()
                        .mapToDouble(t -> t.getLongitude() != null ? t.getLongitude() : 0.0)
                        .average()
                        .orElse(0.0);
                
                double avgRssi = groupData.stream()
                        .mapToInt(WifiTelemetry::getRssi)
                        .average()
                        .orElse(0.0);

                recommendations.put(signalCategory, PositionRecommendation.builder()
                        .signalCategory(signalCategory)
                        .latitude(avgLat)
                        .longitude(avgLon)
                        .averageRssi(avgRssi)
                        .measurementCount(groupData.size())
                        .build());
            }
        }

        return PositioningRecommendations.builder()
                .recommendations(recommendations)
                .bestPosition(recommendations.get("EXCELLENT"))
                .improvementSuggestions(generateImprovementSuggestions(recommendations))
                .build();
    }

    /**
     * Generate improvement suggestions.
     */
    private List<String> generateImprovementSuggestions(Map<String, PositionRecommendation> recommendations) {
        List<String> suggestions = new ArrayList<>();
        
        PositionRecommendation excellent = recommendations.get("EXCELLENT");
        PositionRecommendation good = recommendations.get("GOOD");
        PositionRecommendation fair = recommendations.get("FAIR");
        
        if (excellent != null) {
            suggestions.add("Move to areas with excellent signal strength around coordinates (" + 
                           excellent.getLatitude() + ", " + excellent.getLongitude() + ")");
        } else if (good != null) {
            suggestions.add("Move to areas with good signal strength around coordinates (" + 
                           good.getLatitude() + ", " + good.getLongitude() + ")");
        } else if (fair != null) {
            suggestions.add("Move to areas with fair signal strength around coordinates (" + 
                           fair.getLatitude() + ", " + fair.getLongitude() + ")");
        }
        
        suggestions.add("Avoid areas with very poor signal strength");
        suggestions.add("Consider router placement optimization for better coverage");
        
        return suggestions;
    }

    /**
     * Calculate distance between two geographic coordinates.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in meters
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Validate heat map request.
     */
    private void validateHeatMapRequest(HeatMapRequest request) {
        if (request.getGridSizeMeters() != null && request.getGridSizeMeters() < 1) {
            throw new InvalidHeatMapDataException("Grid size must be at least 1 meter");
        }
        
        if (request.getGridSizeMeters() != null && request.getGridSizeMeters() > 1000) {
            throw new InvalidHeatMapDataException("Grid size cannot exceed 1000 meters");
        }
        
        if (request.getRadiusMeters() != null && request.getRadiusMeters() < 10) {
            throw new InvalidHeatMapDataException("Radius must be at least 10 meters");
        }
        
        if (request.getRadiusMeters() != null && request.getRadiusMeters() > 10000) {
            throw new InvalidHeatMapDataException("Radius cannot exceed 10000 meters");
        }
    }

    /**
     * Heat map grid data class.
     */
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
    public static class PositioningRecommendations {
        private Map<String, PositionRecommendation> recommendations;
        private PositionRecommendation bestPosition;
        private List<String> improvementSuggestions;

        public static PositioningRecommendations empty() {
            PositioningRecommendations recs = new PositioningRecommendations();
            recs.recommendations = new HashMap<>();
            recs.bestPosition = null;
            recs.improvementSuggestions = new ArrayList<>();
            return recs;
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