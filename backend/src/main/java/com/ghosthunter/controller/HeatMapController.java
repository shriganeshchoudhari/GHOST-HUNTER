package com.ghosthunter.controller;

import com.ghosthunter.dto.HeatMapRequest;
import com.ghosthunter.dto.HeatMapResponse;
import com.ghosthunter.dto.HeatMapStatistics;
import com.ghosthunter.service.HeatMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for heat map operations.
 */
@RestController
@RequestMapping("/api/v1/heatmaps")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HeatMapController {

    private final HeatMapService heatMapService;

    /**
     * Generate a heat map for the current user.
     *
     * @param principal Current authenticated user
     * @param request Heat map generation request
     * @return Generated heat map response
     */
    @PostMapping("/generate")
    public ResponseEntity<HeatMapResponse> generateHeatMap(
            Principal principal,
            @Valid @RequestBody HeatMapRequest request) {
        
        log.info("Heat map generation request from user: {} with request: {}", principal.getName(), request);

        try {
            UUID userId = UUID.fromString(principal.getName());
            HeatMapResponse response = heatMapService.generateHeatMap(userId, request);

            log.info("Successfully generated heat map for user: {}", principal.getName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating heat map for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get heat map statistics for the current user.
     *
     * @param principal Current authenticated user
     * @param request Heat map request
     * @return Heat map statistics
     */
    @PostMapping("/statistics")
    public ResponseEntity<HeatMapStatistics> getHeatMapStatistics(
            Principal principal,
            @Valid @RequestBody HeatMapRequest request) {
        
        log.info("Heat map statistics request from user: {}", principal.getName());

        try {
            UUID userId = UUID.fromString(principal.getName());
            HeatMapStatistics statistics = heatMapService.getHeatMapStatistics(userId, request);

            log.info("Retrieved heat map statistics for user: {}", principal.getName());
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error getting heat map statistics for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get saved heat maps for the current user.
     *
     * @param principal Current authenticated user
     * @param limit Number of heat maps to return
     * @return List of saved heat map responses
     */
    @GetMapping("/saved")
    public ResponseEntity<List<HeatMapResponse>> getSavedHeatMaps(
            Principal principal,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Saved heat maps request from user: {} with limit: {}", principal.getName(), limit);

        try {
            UUID userId = UUID.fromString(principal.getName());
            List<HeatMapResponse> responses = heatMapService.getSavedHeatMaps(userId, limit);

            log.info("Retrieved {} saved heat maps for user: {}", responses.size(), principal.getName());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error getting saved heat maps for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Save a heat map for the current user.
     *
     * @param principal Current authenticated user
     * @param heatMapResponse Heat map to save
     * @return Saved heat map response
     */
    @PostMapping("/save")
    public ResponseEntity<HeatMapResponse> saveHeatMap(
            Principal principal,
            @RequestBody HeatMapResponse heatMapResponse) {
        
        log.info("Save heat map request from user: {}", principal.getName());

        try {
            UUID userId = UUID.fromString(principal.getName());
            HeatMapResponse response = heatMapService.saveHeatMap(userId, heatMapResponse);

            log.info("Successfully saved heat map for user: {}", principal.getName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving heat map for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get optimal WiFi positioning recommendations.
     *
     * @param principal Current authenticated user
     * @param request Heat map request
     * @return Positioning recommendations
     */
    @PostMapping("/recommendations")
    public ResponseEntity<HeatMapService.PositioningRecommendations> getOptimalPositioningRecommendations(
            Principal principal,
            @Valid @RequestBody HeatMapRequest request) {
        
        log.info("Positioning recommendations request from user: {}", principal.getName());

        try {
            UUID userId = UUID.fromString(principal.getName());
            HeatMapService.PositioningRecommendations recommendations = 
                    heatMapService.getOptimalPositioningRecommendations(userId, request);

            log.info("Retrieved positioning recommendations for user: {}", principal.getName());
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error getting positioning recommendations for user: {}", principal.getName(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}