package com.ghosthunter.controller;

import com.ghosthunter.dto.LoginRequest;
import com.ghosthunter.dto.LoginResponse;
import com.ghosthunter.dto.RefreshTokenRequest;
import com.ghosthunter.dto.UserRegistrationRequest;
import com.ghosthunter.dto.UserResponse;
import com.ghosthunter.service.AuthenticationService;
import com.ghosthunter.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    /**
     * Register a new user account.
     *
     * @param request User registration request
     * @return Created user response
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("User registration request for email: {}", request.getEmail());

        UserResponse userResponse = userService.register(request);

        log.info("User registered successfully: {}", userResponse.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * Authenticate user and return JWT tokens.
     *
     * @param request Login request with email and password
     * @return Login response with access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login request for email: {}", request.getEmail());

        LoginResponse loginResponse = authenticationService.login(request);

        log.info("User logged in successfully: {}", loginResponse.getUser().getId());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param request Refresh token request
     * @return Login response with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");

        LoginResponse loginResponse = authenticationService.refreshToken(request);

        log.info("Token refreshed successfully for user: {}", loginResponse.getUser().getId());
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Logout user.
     * Note: In a stateless JWT system, this is mainly for logging purposes.
     *
     * @param userId User ID from JWT token
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestAttribute("userId") String userId) {
        log.info("User logout request for: {}", userId);

        try {
            java.util.UUID userUUID = java.util.UUID.fromString(userId);
            authenticationService.logout(userUUID);

            log.info("User logged out successfully: {}", userId);
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            log.error("Logout failed for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Logout failed");
        }
    }
}