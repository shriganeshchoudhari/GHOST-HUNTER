package com.ghosthunter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity representing a GHOST-HUNTER application user.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_subscription_tier", columnList = "subscription_tier")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 255)
    private String fullName;

    @Column(length = 512)
    private String avatarUrl;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SubscriptionTier subscriptionTier;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Column
    private LocalDateTime subscriptionStartedAt;

    @Column
    private LocalDateTime subscriptionExpiresAt;

    @Column(nullable = false)
    private Boolean emailVerified;

    @Column
    private LocalDateTime emailVerifiedAt;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "jsonb")
    private String preferences;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    /**
     * Subscription tier enumeration.
     */
    public enum SubscriptionTier {
        FREE,
        PREMIUM,
        ENTERPRISE
    }

    /**
     * Subscription status enumeration.
     */
    public enum SubscriptionStatus {
        ACTIVE,
        INACTIVE,
        CANCELLED
    }

    /**
     * Check if user account is active.
     */
    public boolean isActive() {
        return deletedAt == null && subscriptionStatus == SubscriptionStatus.ACTIVE;
    }

    /**
     * Check if user email is verified.
     */
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }

    /**
     * Check if subscription is active.
     */
    public boolean hasActiveSubscription() {
        return subscriptionStatus == SubscriptionStatus.ACTIVE &&
               (subscriptionExpiresAt == null || subscriptionExpiresAt.isAfter(LocalDateTime.now()));
    }
}
