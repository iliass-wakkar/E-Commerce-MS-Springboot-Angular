package com.Gateway.Server.controller;

import com.Gateway.Server.dto.LoginRequest;
import com.Gateway.Server.dto.LoginResponse;
import com.Gateway.Server.dto.UserDTO;
import com.Gateway.Server.exception.UnauthorizedException;
import com.Gateway.Server.service.TokenService;
import com.Gateway.Server.service.UserServiceClientReactive;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceClientReactive userServiceClient;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(
            userServiceClient.getUserByEmail(loginRequest.getEmail())
                .flatMap(user -> {
                    // Validate password using BCrypt
                    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        return Mono.error(new UnauthorizedException("Invalid email or password"));
                    }

                    // Generate JWT token
                    String token = tokenService.generateToken(user.getId(), user.getEmail(), user.getRole());

                    // Store token info (for logout/revocation tracking)
                    tokenService.storeToken(token, user.getId(), user.getEmail(), user.getRole());

                    // Build response with expiration time
                    LoginResponse response = LoginResponse.builder()
                            .token(token)
                            .userId(user.getId())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .expiresIn(tokenService.getTokenExpirationTime())
                            .build();

                    return Mono.just(ResponseEntity.ok(response));
                })
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid email or password")))
                .onErrorResume(UnauthorizedException.class, Mono::error)
                .onErrorResume(e -> Mono.error(new UnauthorizedException("Authentication failed: " + e.getMessage()))),
            this::loginFallback
        );
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, String>>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return Mono.fromCallable(() -> {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Missing or invalid Authorization header");
            }

            // Extract token from "Bearer <token>"
            String token = authHeader.replace("Bearer ", "");

            // Invalidate token
            tokenService.invalidateToken(token);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            return ResponseEntity.ok(response);
        })
        .onErrorResume(e -> Mono.error(new UnauthorizedException("Logout failed: " + e.getMessage())));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserDTO>> register(@RequestBody UserDTO userDTO) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(
            Mono.fromCallable(() -> {
                // Hash password before sending to Client-MS
                userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

                // Set default role if not provided
                if (userDTO.getRole() == null || userDTO.getRole().isEmpty()) {
                    userDTO.setRole("CLIENT");
                }
                return userDTO;
            })
            .flatMap(userServiceClient::createUser)
            .map(createdUser -> {
                createdUser.setPassword(null);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
            })
            .onErrorResume(e -> Mono.error(new RuntimeException("Registration failed: " + e.getMessage()))),
            this::registerFallback
        );
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserDTO>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("userService");

        return circuitBreaker.run(
            Mono.fromCallable(() -> {
                // Validate Authorization header
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    throw new UnauthorizedException("Missing or invalid Authorization header");
                }

                // Extract token
                String token = authHeader.replace("Bearer ", "");

                // Validate token
                if (!tokenService.validateToken(token)) {
                    throw new UnauthorizedException("Invalid or expired token");
                }

                // Get user info from token
                var tokenInfo = tokenService.getUserFromToken(token);

                if (tokenInfo == null) {
                    throw new UnauthorizedException("Invalid token");
                }

                return tokenInfo.getUserId();
            })
            .flatMap(userServiceClient::getUserById)
            .map(user -> {
                // Remove password from response
                user.setPassword(null);
                return ResponseEntity.ok(user);
            })
            .onErrorResume(UnauthorizedException.class, Mono::error)
            .onErrorResume(e -> Mono.error(new RuntimeException("Failed to get user info: " + e.getMessage()))),
            this::getCurrentUserFallback
        );
    }

    private Mono<ResponseEntity<LoginResponse>> loginFallback(Throwable throwable) {
        if (throwable instanceof UnauthorizedException) {
            return Mono.error(throwable);
        }
        return Mono.error(new RuntimeException(
            "Authentication Service is currently unavailable, please try again later."
        ));
    }

    private Mono<ResponseEntity<UserDTO>> registerFallback(Throwable throwable) {
        return Mono.error(new RuntimeException(
            "User Registration Service is currently unavailable, please try again later."
        ));
    }

    private Mono<ResponseEntity<UserDTO>> getCurrentUserFallback(Throwable throwable) {
        if (throwable instanceof UnauthorizedException) {
            return Mono.error(throwable);
        }
        return Mono.error(new RuntimeException(
            "User Service is currently unavailable, please try again later."
        ));
    }
}

