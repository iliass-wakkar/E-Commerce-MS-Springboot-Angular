package com.Gateway.Server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Spring Security configuration for JWT-based authentication
 * Integrates with reactive WebFlux and JWT filter
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // Public endpoints that don't require JWT authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/auth/login",
        "/auth/register",
        "/actuator/**",
        "/error"
    };

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            // Disable CSRF for microservices
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            // Configure authorization
            .authorizeExchange(exchanges -> exchanges
                // Allow OPTIONS for CORS preflight
                .pathMatchers(HttpMethod.OPTIONS).permitAll()

                // Public endpoints - no authentication required
                .pathMatchers(PUBLIC_ENDPOINTS).permitAll()

                // Protected endpoints - require authentication via JWT
                .pathMatchers("/auth/me").authenticated()
                .pathMatchers("/auth/logout").authenticated()

                // All other requests are allowed (will be handled by gateway routes)
                .anyExchange().permitAll()
            )

            // Disable form login (we use JWT)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

            // Disable HTTP Basic auth (we use JWT)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

            .build();
    }
}

