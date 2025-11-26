package com.Gateway.Server.filters;

import com.Gateway.Server.model.TokenInfo;
import com.Gateway.Server.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private TokenService tokenService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // Extract token
        String token = authHeader.replace("Bearer ", "");

        // Validate token
        if (!tokenService.validateToken(token)) {
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        // Get user info from token
        TokenInfo tokenInfo = tokenService.getUserFromToken(token);

        if (tokenInfo == null) {
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }

        // Add user info to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(tokenInfo.getUserId()))
                .header("X-User-Email", tokenInfo.getEmail())
                .header("X-User-Role", tokenInfo.getRole())
                .build();

        // Continue with modified request
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format("{\"error\":\"%s\",\"message\":\"%s\"}",
                httpStatus.getReasonPhrase(), message);

        DataBuffer buffer = response.bufferFactory()
                .wrap(errorResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}

