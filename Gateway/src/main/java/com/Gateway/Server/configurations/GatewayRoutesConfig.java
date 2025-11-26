package com.Gateway.Server.configurations;

import com.Gateway.Server.filters.AdminAuthorizationFilter;
import com.Gateway.Server.filters.AuthenticationFilter;
import com.Gateway.Server.filters.CustomGatewayFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Autowired
    private CustomGatewayFilter customGatewayFilter;

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Autowired
    private AdminAuthorizationFilter adminAuthorizationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Public route: Authentication endpoints (no auth required)
                .route("auth-route", r -> r
                        .path("/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Request-Origin", "Gateway")
                        )
                        .uri("forward:///") // Handle locally by AuthController
                )

                // Public route: Browse products (GET only - no auth required)
                .route("product-service-public-route", r -> r
                        .path("/product-service/**")
                        .and()
                        .method("GET")
                        .filters(f -> f
                                .rewritePath("/product-service/(?<remaining>.*)", "/${remaining}")
                                .addRequestHeader("X-Request-Origin", "Gateway")
                                .filter(customGatewayFilter)
                        )
                        .uri("lb://product-service")
                )

                // Protected route: Product service (POST, PUT, DELETE - auth required)
                .route("product-service-protected-route", r -> r
                        .path("/product-service/**")
                        .and()
                        .method("POST", "PUT", "DELETE")
                        .filters(f -> f
                                .rewritePath("/product-service/(?<remaining>.*)", "/${remaining}")
                                .addRequestHeader("X-Request-Origin", "Gateway")
                                .filter(authenticationFilter) // Require authentication
                                .filter(customGatewayFilter)
                        )
                        .uri("lb://product-service")
                )

                // Admin route: Get all users (admin only)
                .route("ms-client-admin-getall-route", r -> r
                        .path("/MS-CLIENT/users")
                        .and()
                        .method("GET")
                        .filters(f -> f
                                .rewritePath("/MS-CLIENT/(?<remaining>.*)", "/api/v1/${remaining}")
                                .filter(authenticationFilter) // Validate token first
                                .filter(adminAuthorizationFilter) // Then check admin role
                        )
                        .uri("lb://MS-CLIENT")
                )

                // Admin route: Update/Delete user (admin only)
                .route("ms-client-admin-modify-route", r -> r
                        .path("/MS-CLIENT/users/**")
                        .and()
                        .method("PUT", "DELETE")
                        .filters(f -> f
                                .rewritePath("/MS-CLIENT/(?<remaining>.*)", "/api/v1/${remaining}")
                                .filter(authenticationFilter) // Validate token first
                                .filter(adminAuthorizationFilter) // Then check admin role
                        )
                        .uri("lb://MS-CLIENT")
                )

                // Protected route: Client-MS user endpoints (authenticated users)
                .route("ms-client-protected-route", r -> r
                        .path("/MS-CLIENT/users/**")
                        .filters(f -> f
                                .rewritePath("/MS-CLIENT/(?<remaining>.*)", "/api/v1/${remaining}")
                                .filter(authenticationFilter) // Require authentication
                        )
                        .uri("lb://MS-CLIENT")
                )

                // Public route: Get user by email (used by login)
                .route("ms-client-public-route", r -> r
                        .path("/MS-CLIENT/users/email/**")
                        .filters(f -> f
                                .rewritePath("/MS-CLIENT/(?<remaining>.*)", "/api/v1/${remaining}")
                        )
                        .uri("lb://MS-CLIENT")
                )

                .build();
    }
}
