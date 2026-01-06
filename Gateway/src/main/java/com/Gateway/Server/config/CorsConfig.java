package com.Gateway.Server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for reactive gateway
 * Handles preflight OPTIONS requests and allows Authorization headers
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Allow frontend origin
        corsConfig.setAllowedOrigins(List.of("http://localhost:4200"));

        // Allow all headers (including Authorization, Content-Type, etc.)
        corsConfig.setAllowedHeaders(Arrays.asList("*"));

        // Allow all HTTP methods (GET, POST, PUT, DELETE, OPTIONS, etc.)
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Expose Authorization header to frontend
        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Max age for preflight cache (1 hour)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

