package com.ghosthunter.controller;

import com.ghosthunter.dto.UserResponse;
import com.ghosthunter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile.
     *
     * @param principal Current authenticated user
     * @return User profile response
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Principal principal) {
        log.info("Get profile request for user: {}", principal.getName());

        UserResponse userResponse = userService.findByEmail(principal.getName());

        log.info("Profile retrieved successfully for user: {}", userResponse.getId());
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Update user profile.
     *
     * @param principal Current authenticated user
     * @param fullName Full name
     * @param bio Biography
     * @param country Country
     * @param region Region
     * @return Updated user profile response
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {

        log.info("Update profile request for user: {}", principal.getName());

        UserResponse userResponse = userService.updateProfile(
                UUID.fromString(principal.getName()),
                fullName,
                bio,
                country,
                region
        );

        log.info("Profile updated successfully for user: {}", userResponse.getId());
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Get user statistics.
     *
     * @param principal Current authenticated user
     * @return User statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<UserService.UserStatistics> getStatistics(Principal principal) {
        log.info("Get statistics request for user: {}", principal.getName());

        UserService.UserStatistics statistics = userService.getUserStatistics(
                UUID.fromString(principal.getName())
        );

        log.info("Statistics retrieved successfully for user: {}", principal.getName());
        return ResponseEntity.ok(statistics);
    }

    /**
     * Upgrade user subscription tier.
     *
     * @param principal Current authenticated user
     * @param tier New subscription tier
     * @return Updated user profile response
     */
    @PostMapping("/subscription")
    public ResponseEntity<UserResponse> upgradeSubscription(
            Principal principal,
            @RequestParam String tier) {

        log.info("Subscription upgrade request for user: {} to tier: {}", principal.getName(), tier);

        try {
            com.ghosthunter.model.User.SubscriptionTier subscriptionTier = 
                com.ghosthunter.model.User.SubscriptionTier.valueOf(tier.toUpperCase());

            UserResponse userResponse = userService.updateSubscriptionTier(
                    UUID.fromString(principal.getName()),
                    subscriptionTier
            );

            log.info("Subscription upgraded successfully for user: {}", userResponse.getId());
            return ResponseEntity.ok(userResponse);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid subscription tier: {}", tier);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verify user email.
     * Note: In a real implementation, this would be triggered by email verification link.
     *
     * @param principal Current authenticated user
     * @return Updated user profile response
     */
    @PostMapping("/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(Principal principal) {
        log.info("Email verification request for user: {}", principal.getName());

        UserResponse userResponse = userService.verifyEmail(
                UUID.fromString(principal.getName())
        );

        log.info("Email verified successfully for user: {}", userResponse.getId());
        return ResponseEntity.ok(userResponse);
    }
}