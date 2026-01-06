package com.Gateway.Server.filters;

import com.Gateway.Server.exception.JwtException;
import com.Gateway.Server.exception.UnauthorizedException;
import com.Gateway.Server.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive JWT authentication filter
 * Validates JWT token from Authorization header and extracts claims
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();

        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(requestPath)) {
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // No token provided - continue to next filter (SecurityConfig will handle authorization)
            return chain.filter(exchange);
        }

        try {
            String token = authHeader.replace(BEARER_PREFIX, "");

            // Validate JWT token
            Claims claims = jwtTokenProvider.validateToken(token);

            // Extract claims and add as attributes
            Long userId = ((Number) claims.get("userId")).longValue();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // Store in request attributes for downstream use
            exchange.getAttributes().put("userId", userId);
            exchange.getAttributes().put("userEmail", email);
            exchange.getAttributes().put("userRole", role);

            // Add to request headers for microservices to use
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(userId))
                            .header("X-User-Email", email)
                            .header("X-User-Role", role)
                            .build())
                    .build();

            log.debug("JWT validated for user: {} with role: {}", email, role);
            return chain.filter(modifiedExchange);

        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            // Continue to next filter - UnauthorizedException will be handled by SecurityConfig
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
            return chain.filter(exchange);
        }
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String path) {
        return path.equals("/auth/login") ||
                path.equals("/auth/register") ||
                path.equals("/auth/logout") ||
                path.startsWith("/actuator/") ||
                path.equals("/error");
    }
}

