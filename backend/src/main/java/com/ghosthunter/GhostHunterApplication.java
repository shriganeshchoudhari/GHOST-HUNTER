package com.ghosthunter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Main Spring Boot application class for GHOST-HUNTER backend.
 * 
 * This application provides REST APIs for:
 * - User authentication and profile management
 * - WiFi telemetry data collection and processing
 * - Heat map generation and visualization
 * - Analytics and optimization recommendations
 * - Social features and leaderboards
 * - Community data aggregation
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class GhostHunterApplication {

    public static void main(String[] args) {
        SpringApplication.run(GhostHunterApplication.class, args);
    }

    /**
     * Configure CORS (Cross-Origin Resource Sharing) for mobile and web clients.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:19006",
            "http://localhost:8081",
            "https://ghosthunter.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
