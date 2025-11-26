package com.Gateway.Server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {
    private Long userId;
    private String email;
    private String role; // CLIENT or ADMIN
    private Instant createdAt;
}

