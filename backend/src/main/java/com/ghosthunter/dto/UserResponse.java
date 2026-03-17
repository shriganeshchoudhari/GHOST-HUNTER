package com.ghosthunter.dto;

import com.ghosthunter.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for user responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String subscriptionTier;
    private String subscriptionStatus;
    private LocalDateTime subscriptionStartedAt;
    private LocalDateTime subscriptionExpiresAt;
    private Boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private String phoneNumber;
    private String country;
    private String region;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert User entity to UserResponse DTO.
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .subscriptionTier(user.getSubscriptionTier().name())
                .subscriptionStatus(user.getSubscriptionStatus().name())
                .subscriptionStartedAt(user.getSubscriptionStartedAt())
                .subscriptionExpiresAt(user.getSubscriptionExpiresAt())
                .emailVerified(user.isEmailVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .phoneNumber(user.getPhoneNumber())
                .country(user.getCountry())
                .region(user.getRegion())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}