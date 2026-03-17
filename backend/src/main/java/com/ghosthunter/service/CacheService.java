package com.ghosthunter.service;

import com.ghosthunter.dto.HeatMapResponse;
import com.ghosthunter.dto.TelemetryStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing Redis caching operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Cache keys
    private static final String USER_TELEMETRY_STATS_PREFIX = "user:telemetry:stats:";
    private static final String USER_HEATMAP_PREFIX = "user:heatmap:";
    private static final String USER_PROFILE_PREFIX = "user:profile:";
    
    // Cache TTLs
    private static final Duration TELEMETRY_STATS_TTL = Duration.ofMinutes(15);
    private static final Duration HEATMAP_TTL = Duration.ofHours(1);
    private static final Duration USER_PROFILE_TTL = Duration.ofMinutes(30);

    /**
     * Cache telemetry statistics for a user.
     */
    public void cacheTelemetryStatistics(String userId, TelemetryStatistics statistics) {
        try {
            String key = USER_TELEMETRY_STATS_PREFIX + userId;
            redisTemplate.opsForValue().set(key, statistics, TELEMETRY_STATS_TTL.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Cached telemetry statistics for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to cache telemetry statistics for user: {}", userId, e);
        }
    }

    /**
     * Get cached telemetry statistics for a user.
     */
    public TelemetryStatistics getCachedTelemetryStatistics(String userId) {
        try {
            String key = USER_TELEMETRY_STATS_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof TelemetryStatistics) {
                log.debug("Retrieved cached telemetry statistics for user: {}", userId);
                return (TelemetryStatistics) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached telemetry statistics for user: {}", userId, e);
        }
        return null;
    }

    /**
     * Cache heat map for a user.
     */
    public void cacheHeatMap(String userId, HeatMapResponse heatMap) {
        try {
            String key = USER_HEATMAP_PREFIX + userId + ":" + heatMap.getGeneratedAt().toString();
            redisTemplate.opsForValue().set(key, heatMap, HEATMAP_TTL.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Cached heat map for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to cache heat map for user: {}", userId, e);
        }
    }

    /**
     * Get cached heat map for a user.
     */
    public HeatMapResponse getCachedHeatMap(String userId, String cacheKey) {
        try {
            String key = USER_HEATMAP_PREFIX + userId + ":" + cacheKey;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof HeatMapResponse) {
                log.debug("Retrieved cached heat map for user: {}", userId);
                return (HeatMapResponse) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached heat map for user: {}", userId, e);
        }
        return null;
    }

    /**
     * Cache user profile.
     */
    public void cacheUserProfile(String userId, Object userProfile) {
        try {
            String key = USER_PROFILE_PREFIX + userId;
            redisTemplate.opsForValue().set(key, userProfile, USER_PROFILE_TTL.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Cached user profile for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to cache user profile for user: {}", userId, e);
        }
    }

    /**
     * Get cached user profile.
     */
    public Object getCachedUserProfile(String userId) {
        try {
            String key = USER_PROFILE_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Retrieved cached user profile for user: {}", userId);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached user profile for user: {}", userId, e);
        }
        return null;
    }

    /**
     * Invalidate all caches for a user.
     */
    public void invalidateUserCaches(String userId) {
        try {
            // Delete telemetry statistics cache
            String telemetryKey = USER_TELEMETRY_STATS_PREFIX + userId;
            redisTemplate.delete(telemetryKey);
            
            // Delete heat map caches
            String heatMapPattern = USER_HEATMAP_PREFIX + userId + ":*";
            redisTemplate.keys(heatMapPattern).forEach(key -> redisTemplate.delete(key));
            
            // Delete user profile cache
            String profileKey = USER_PROFILE_PREFIX + userId;
            redisTemplate.delete(profileKey);
            
            log.debug("Invalidated all caches for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate caches for user: {}", userId, e);
        }
    }

    /**
     * Invalidate telemetry statistics cache for a user.
     */
    public void invalidateTelemetryStatsCache(String userId) {
        try {
            String key = USER_TELEMETRY_STATS_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("Invalidated telemetry statistics cache for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate telemetry statistics cache for user: {}", userId, e);
        }
    }

    /**
     * Invalidate heat map cache for a user.
     */
    public void invalidateHeatMapCache(String userId) {
        try {
            String pattern = USER_HEATMAP_PREFIX + userId + ":*";
            redisTemplate.keys(pattern).forEach(key -> redisTemplate.delete(key));
            log.debug("Invalidated heat map cache for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate heat map cache for user: {}", userId, e);
        }
    }

    /**
     * Check if telemetry statistics are cached for a user.
     */
    public boolean isTelemetryStatsCached(String userId) {
        try {
            String key = USER_TELEMETRY_STATS_PREFIX + userId;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Failed to check telemetry stats cache for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Check if user profile is cached.
     */
    public boolean isUserProfileCached(String userId) {
        try {
            String key = USER_PROFILE_PREFIX + userId;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Failed to check user profile cache for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Get cache statistics.
     */
    public CacheStatistics getCacheStatistics() {
        try {
            CacheStatistics stats = new CacheStatistics();
            
            // Count telemetry stats cache entries
            Iterable<String> telemetryKeys = redisTemplate.keys(USER_TELEMETRY_STATS_PREFIX + "*");
            int telemetryCount = 0;
            for (String key : telemetryKeys) {
                telemetryCount++;
            }
            stats.setTelemetryStatsCount(telemetryCount);
            
            // Count heat map cache entries
            Iterable<String> heatMapKeys = redisTemplate.keys(USER_HEATMAP_PREFIX + "*");
            int heatMapCount = 0;
            for (String key : heatMapKeys) {
                heatMapCount++;
            }
            stats.setHeatMapCount(heatMapCount);
            
            // Count user profile cache entries
            Iterable<String> profileKeys = redisTemplate.keys(USER_PROFILE_PREFIX + "*");
            int profileCount = 0;
            for (String key : profileKeys) {
                profileCount++;
            }
            stats.setUserProfileCount(profileCount);
            
            return stats;
        } catch (Exception e) {
            log.warn("Failed to get cache statistics", e);
            return new CacheStatistics();
        }
    }

    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        try {
            // Delete all cache entries
            Iterable<String> keys = redisTemplate.keys("*");
            redisTemplate.delete(keys);
            log.info("Cleared all caches");
        } catch (Exception e) {
            log.warn("Failed to clear all caches", e);
        }
    }

    /**
     * Cache statistics data class.
     */
    public static class CacheStatistics {
        private int telemetryStatsCount;
        private int heatMapCount;
        private int userProfileCount;

        // Getters and setters
        public int getTelemetryStatsCount() { return telemetryStatsCount; }
        public void setTelemetryStatsCount(int telemetryStatsCount) { this.telemetryStatsCount = telemetryStatsCount; }
        
        public int getHeatMapCount() { return heatMapCount; }
        public void setHeatMapCount(int heatMapCount) { this.heatMapCount = heatMapCount; }
        
        public int getUserProfileCount() { return userProfileCount; }
        public void setUserProfileCount(int userProfileCount) { this.userProfileCount = userProfileCount; }
    }
}