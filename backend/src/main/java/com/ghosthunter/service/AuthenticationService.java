package com.ghosthunter.service;

import com.ghosthunter.dto.LoginRequest;
import com.ghosthunter.dto.LoginResponse;
import com.ghosthunter.dto.RefreshTokenRequest;
import com.ghosthunter.dto.UserResponse;
import com.ghosthunter.exception.InvalidCredentialsException;
import com.ghosthunter.exception.UserNotFoundException;
import com.ghosthunter.model.User;
import com.ghosthunter.repository.UserRepository;
import com.ghosthunter.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for authentication operations including login, logout, and token management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Authenticate user and generate JWT tokens.
     *
     * @param request Login request containing email and password
     * @return Login response with access and refresh tokens
     * @throws InvalidCredentialsException if authentication fails
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt for email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Login failed: User not found for email: {}", request.getEmail());
                        return new UserNotFoundException("User not found: " + request.getEmail());
                    });

            if (!user.isActive()) {
                log.warn("Login failed: User account is inactive: {}", user.getId());
                throw new InvalidCredentialsException("Account is inactive");
            }

            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // Update last login time
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("User logged in successfully: {}", user.getId());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtUtil.getAccessTokenExpirationTime())
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {}: {}", request.getEmail(), e.getMessage());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param request Refresh token request
     * @return Login response with new access token
     * @throws InvalidCredentialsException if refresh token is invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Token refresh request");

        try {
            // Validate refresh token
            if (!jwtUtil.validateRefreshToken(request.getRefreshToken())) {
                log.warn("Invalid refresh token");
                throw new InvalidCredentialsException("Invalid refresh token");
            }

            // Extract user ID from token
            String userId = jwtUtil.getUserIdFromRefreshToken(request.getRefreshToken());
            UUID userUUID = UUID.fromString(userId);

            // Get user details
            User user = userRepository.findById(userUUID)
                    .orElseThrow(() -> {
                        log.warn("Token refresh failed: User not found: {}", userId);
                        return new UserNotFoundException("User not found: " + userId);
                    });

            if (!user.isActive()) {
                log.warn("Token refresh failed: User account is inactive: {}", user.getId());
                throw new InvalidCredentialsException("Account is inactive");
            }

            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(user);

            log.info("Token refreshed successfully for user: {}", user.getId());

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken()) // Keep same refresh token
                    .expiresIn(jwtUtil.getAccessTokenExpirationTime())
                    .user(UserResponse.fromUser(user))
                    .build();

        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    /**
     * Logout user and invalidate tokens.
     * Note: In a stateless JWT system, we cannot truly invalidate tokens.
     * This method is provided for future implementation with token blacklisting.
     *
     * @param userId User ID
     */
    @Transactional
    public void logout(UUID userId) {
        log.info("User logout request for: {}", userId);

        // In a stateless JWT system, we cannot invalidate tokens server-side
        // Future implementation could use token blacklisting or short-lived tokens
        // For now, we just log the logout attempt

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Logout failed: User not found: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        log.info("User logged out: {}", user.getId());
    }

    /**
     * Get current authenticated user.
     *
     * @return User response
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Current user request failed: No authentication found");
            throw new UserNotFoundException("No authenticated user found");
        }

        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Current user request failed: User not found: {}", email);
                    return new UserNotFoundException("User not found: " + email);
                });

        return UserResponse.fromUser(user);
    }

    /**
     * Validate access token.
     *
     * @param token JWT access token
     * @return true if token is valid, false otherwise
     */
    public boolean validateAccessToken(String token) {
        return jwtUtil.validateAccessToken(token);
    }

    /**
     * Get user ID from access token.
     *
     * @param token JWT access token
     * @return User ID
     */
    public UUID getUserIdFromToken(String token) {
        String userId = jwtUtil.getUserIdFromAccessToken(token);
        return UUID.fromString(userId);
    }
}