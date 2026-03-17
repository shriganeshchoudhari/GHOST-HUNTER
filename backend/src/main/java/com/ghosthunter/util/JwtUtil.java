package com.ghosthunter.util;

import com.ghosthunter.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for JWT token generation and validation.
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long jwtRefreshExpiration;

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generate access token for user.
     *
     * @param user User entity
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("subscriptionTier", user.getSubscriptionTier().name());
        claims.put("roles", "USER");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtExpiration, ChronoUnit.MILLIS)))
                .signWith(key)
                .compact();
    }

    /**
     * Generate refresh token for user.
     *
     * @param user User entity
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("type", "refresh");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtRefreshExpiration, ChronoUnit.MILLIS)))
                .signWith(key)
                .compact();
    }

    /**
     * Validate access token.
     *
     * @param token JWT access token
     * @return true if token is valid, false otherwise
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate refresh token.
     *
     * @param token JWT refresh token
     * @return true if token is valid, false otherwise
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenType = claims.get("type", String.class);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user ID from access token.
     *
     * @param token JWT access token
     * @return User ID as string
     */
    public String getUserIdFromAccessToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", String.class);
    }

    /**
     * Get user ID from refresh token.
     *
     * @param token JWT refresh token
     * @return User ID as string
     */
    public String getUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", String.class);
    }

    /**
     * Get email from token.
     *
     * @param token JWT token
     * @return User email
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Get subscription tier from access token.
     *
     * @param token JWT access token
     * @return Subscription tier
     */
    public String getSubscriptionTierFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("subscriptionTier", String.class);
    }

    /**
     * Get access token expiration time in milliseconds.
     *
     * @return Expiration time in milliseconds
     */
    public long getAccessTokenExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Get refresh token expiration time in milliseconds.
     *
     * @return Expiration time in milliseconds
     */
    public long getRefreshTokenExpirationTime() {
        return jwtRefreshExpiration;
    }
}