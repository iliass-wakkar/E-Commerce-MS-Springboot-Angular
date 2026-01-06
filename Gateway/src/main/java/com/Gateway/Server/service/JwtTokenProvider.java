package com.Gateway.Server.service;

import com.Gateway.Server.exception.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating and validating JWT tokens
 */
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${gateway.jwt.secret-key:your-secret-key-change-this-in-production-min-256-bits}")
    private String secretKey;

    @Value("${gateway.jwt.ttl-seconds:86400}") // 1 day default
    private long tokenTtlSeconds;

    @Value("${gateway.jwt.issuer:gateway-server}")
    private String issuer;

    /**
     * Generate JWT token with user claims
     */
    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        claims.put("userId", userId);

        return createToken(claims, String.valueOf(userId));
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Instant expiryTime = now.plusSeconds(tokenTtlSeconds);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryTime))
                .signWith(key)
                .compact();
    }

    /**
     * Validate JWT token and return claims
     */
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("JWT token is unsupported", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Invalid JWT token format", e);
        } catch (SignatureException e) {
            throw new JwtException("Invalid JWT signature", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT claims string is empty", e);
        } catch (Exception e) {
            throw new JwtException("Token validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extract userId from JWT token
     */
    public Long extractUserId(String token) {
        Claims claims = validateToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return Long.parseLong(userId.toString());
    }

    /**
     * Extract email from JWT token
     */
    public String extractEmail(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Extract role from JWT token
     */
    public String extractRole(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Get token expiration time
     */
    public long getTokenExpirationTime() {
        return tokenTtlSeconds;
    }
}

