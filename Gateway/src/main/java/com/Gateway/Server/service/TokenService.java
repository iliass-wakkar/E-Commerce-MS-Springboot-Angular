package com.Gateway.Server.service;

import com.Gateway.Server.model.TokenInfo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    // In-memory token storage
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    /**
     * Generate a new UUID token
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Store token with user information
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
     * Validate if token exists and is valid
     */
    public boolean validateToken(String token) {
        return tokenStore.containsKey(token);
    }

    /**
     * Get user information from token
     */
    public TokenInfo getUserFromToken(String token) {
        return tokenStore.get(token);
    }

    /**
     * Invalidate (remove) a token
     */
    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }

    /**
     * Get all active tokens count (for monitoring)
     */
    public int getActiveTokenCount() {
        return tokenStore.size();
    }
}

