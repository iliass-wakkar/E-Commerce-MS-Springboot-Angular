package com.Gateway.Server.configurations;

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

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route for product-service
                .route("product-service-route", r -> r
                        .path("/product-service/**") // Matches requests starting with /product-service
                        .filters(f -> f
                                .rewritePath("/product-service/(?<remaining>.*)", "/${remaining}") // Rewrites the path
                                .addRequestHeader("X-Request-Origin", "Gateway") // Adds a custom header
                                .filter(customGatewayFilter) // Applies the custom filter
                        )
                        .uri("lb://product-service") // Forwards to the product-service via service discovery
                )
                // Route for ms-client service
                .route("ms-client-route", r -> r
                        .path("/MS-CLIENT/**")
                        .filters(f -> f
                                .rewritePath("/MS-CLIENT/(?<remaining>.*)", "/api/v1/users/${remaining}")
                        )
                        .uri("lb://MS-CLIENT")
                )
                .build();
    }
}
