package com.ghosthunter.repository;

import com.ghosthunter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address.
     * 
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     * 
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if email exists.
     * 
     * @param email the email address
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists.
     * 
     * @param username the username
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find active users by subscription tier.
     * 
     * @param subscriptionTier the subscription tier
     * @return count of active users with the specified tier
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.subscriptionTier = :tier AND u.deletedAt IS NULL")
    long countActiveUsersByTier(@Param("tier") User.SubscriptionTier subscriptionTier);
}
