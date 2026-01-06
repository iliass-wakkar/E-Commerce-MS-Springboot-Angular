package com.Gateway.Server.service;

import com.Gateway.Server.model.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing tokens - now using JWT instead of UUID
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    // In-memory token storage for logout/revocation (blacklist)
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    /**
     * Scheduled task to clean up expired tokens from the blacklist
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        tokenStore.entrySet().removeIf(entry -> {
            try {
                // If token is invalid (expired), remove it from store
                jwtTokenProvider.validateToken(entry.getKey());
                return false; // Token is valid, keep it
            } catch (Exception e) {
                return true; // Token is expired/invalid, remove it
            }
        });
    }

    /**
     * Generate a new JWT token
     */
    public String generateToken(Long userId, String email, String role) {
        return jwtTokenProvider.generateToken(userId, email, role);
    }

    /**
     * Store token information (for blacklist on logout)
     */
    public void storeToken(String token, Long userId, String email, String role) {
        TokenInfo tokenInfo = TokenInfo.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .createdAt(Instant.now())
                .build();
        tokenStore.put(token, tokenInfo);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            // First check if token is blacklisted (logged out)
            if (tokenStore.containsKey(token) && tokenStore.get(token).isBlacklisted()) {
                return false;
            }
            // Then validate JWT signature and expiration
            jwtTokenProvider.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get user information from JWT token
     */
    public TokenInfo getUserFromToken(String token) {
        try {
            Long userId = jwtTokenProvider.extractUserId(token);
            String email = jwtTokenProvider.extractEmail(token);
            String role = jwtTokenProvider.extractRole(token);

            return TokenInfo.builder()
                    .userId(userId)
                    .email(email)
                    .role(role)
                    .createdAt(Instant.now())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Invalidate (blacklist) a token
     */
    public void invalidateToken(String token) {
        if (tokenStore.containsKey(token)) {
            TokenInfo info = tokenStore.get(token);
            info.setBlacklisted(true);
        }
    }

    /**
     * Get all active tokens count (for monitoring)
     */
    public int getActiveTokenCount() {
        return tokenStore.size();
    }

    /**
     * Get token expiration time in seconds
     */
    public long getTokenExpirationTime() {
        return jwtTokenProvider.getTokenExpirationTime();
    }
}

