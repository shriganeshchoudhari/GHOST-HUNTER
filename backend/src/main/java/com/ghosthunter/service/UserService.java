package com.ghosthunter.service;

import com.ghosthunter.dto.UserRegistrationRequest;
import com.ghosthunter.dto.UserResponse;
import com.ghosthunter.exception.DuplicateEmailException;
import com.ghosthunter.exception.DuplicateUsernameException;
import com.ghosthunter.exception.UserNotFoundException;
import com.ghosthunter.model.User;
import com.ghosthunter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for user management operations including registration, authentication, and profile management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user account.
     *
     * @param request User registration request containing email, username, and password
     * @return Created user response
     * @throws DuplicateEmailException if email already exists
     * @throws DuplicateUsernameException if username already exists
     */
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new DuplicateEmailException("Email already registered: " + request.getEmail());
        }

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username already exists: {}", request.getUsername());
            throw new DuplicateUsernameException("Username already taken: " + request.getUsername());
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .subscriptionTier(User.SubscriptionTier.FREE)
                .subscriptionStatus(User.SubscriptionStatus.ACTIVE)
                .emailVerified(false)
                .country(request.getCountry())
                .region(request.getRegion())
                .bio(request.getBio())
                .preferences("{}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        return UserResponse.fromUser(savedUser);
    }

    /**
     * Find user by ID.
     *
     * @param userId User ID
     * @return User response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse findById(UUID userId) {
        log.debug("Finding user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });
        
        return UserResponse.fromUser(user);
    }

    /**
     * Find user by email.
     *
     * @param email User email
     * @return User response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });
        
        return UserResponse.fromUser(user);
    }

    /**
     * Find user by username.
     *
     * @param username User username
     * @return User response
     * @throws UserNotFoundException if user not found
     */
    public UserResponse findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UserNotFoundException("User not found with username: " + username);
                });
        
        return UserResponse.fromUser(user);
    }

    /**
     * Update user profile.
     *
     * @param userId User ID
     * @param fullName Full name
     * @param bio Biography
     * @param country Country
     * @param region Region
     * @return Updated user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public UserResponse updateProfile(UUID userId, String fullName, String bio, String country, String region) {
        log.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Profile update failed: User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        // Update user fields
        if (fullName != null) user.setFullName(fullName);
        if (bio != null) user.setBio(bio);
        if (country != null) user.setCountry(country);
        if (region != null) user.setRegion(region);
        
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        
        log.info("Profile updated successfully for user: {}", userId);
        
        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Update user subscription tier.
     *
     * @param userId User ID
     * @param tier New subscription tier
     * @return Updated user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public UserResponse updateSubscriptionTier(UUID userId, User.SubscriptionTier tier) {
        log.info("Updating subscription tier for user: {} to: {}", userId, tier);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Subscription update failed: User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        user.setSubscriptionTier(tier);
        user.setSubscriptionStartedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        
        log.info("Subscription tier updated successfully for user: {}", userId);
        
        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Verify user email.
     *
     * @param userId User ID
     * @return Updated user response
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public UserResponse verifyEmail(UUID userId) {
        log.info("Verifying email for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Email verification failed: User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        
        log.info("Email verified successfully for user: {}", userId);
        
        return UserResponse.fromUser(updatedUser);
    }

    /**
     * Check if user exists by email.
     *
     * @param email User email
     * @return true if user exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if user exists by username.
     *
     * @param username User username
     * @return true if user exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Get user statistics for dashboard.
     *
     * @param userId User ID
     * @return User statistics
     * @throws UserNotFoundException if user not found
     */
    public UserStatistics getUserStatistics(UUID userId) {
        log.debug("Getting statistics for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Statistics request failed: User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        // TODO: Calculate statistics from telemetry data
        // This will be implemented in Phase 3 when telemetry service is available
        
        return UserStatistics.builder()
                .userId(userId)
                .totalSessions(0)
                .totalHuntingTimeMinutes(0)
                .ghostsEncountered(0)
                .ghostsCaptured(0)
                .safeZonesDiscovered(0)
                .currentScore(0)
                .build();
    }

    /**
     * User statistics data transfer object.
     */
    public static class UserStatistics {
        private UUID userId;
        private int totalSessions;
        private int totalHuntingTimeMinutes;
        private int ghostsEncountered;
        private int ghostsCaptured;
        private int safeZonesDiscovered;
        private int currentScore;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public int getTotalSessions() { return totalSessions; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        public int getTotalHuntingTimeMinutes() { return totalHuntingTimeMinutes; }
        public void setTotalHuntingTimeMinutes(int totalHuntingTimeMinutes) { this.totalHuntingTimeMinutes = totalHuntingTimeMinutes; }
        public int getGhostsEncountered() { return ghostsEncountered; }
        public void setGhostsEncountered(int ghostsEncountered) { this.ghostsEncountered = ghostsEncountered; }
        public int getGhostsCaptured() { return ghostsCaptured; }
        public void setGhostsCaptured(int ghostsCaptured) { this.ghostsCaptured = ghostsCaptured; }
        public int getSafeZonesDiscovered() { return safeZonesDiscovered; }
        public void setSafeZonesDiscovered(int safeZonesDiscovered) { this.safeZonesDiscovered = safeZonesDiscovered; }
        public int getCurrentScore() { return currentScore; }
        public void setCurrentScore(int currentScore) { this.currentScore = currentScore; }
    }
}