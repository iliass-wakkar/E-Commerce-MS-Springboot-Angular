package com.Gateway.Server.controller;

import com.Gateway.Server.dto.LoginRequest;
import com.Gateway.Server.dto.LoginResponse;
import com.Gateway.Server.dto.UserDTO;
import com.Gateway.Server.exception.UnauthorizedException;
import com.Gateway.Server.service.TokenService;
import com.Gateway.Server.service.UserServiceClientReactive;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return userServiceClient.getUserByEmail(loginRequest.getEmail())
            .flatMap(user -> {
                // Validate password using BCrypt
                if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                    return Mono.error(new UnauthorizedException("Invalid email or password"));
                }

                // Generate token
                String token = tokenService.generateToken();

                // Store token with user info
                tokenService.storeToken(token, user.getId(), user.getEmail(), user.getRole());

                // Build response
                LoginResponse response = LoginResponse.builder()
                        .token(token)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build();

                return Mono.just(ResponseEntity.ok(response));
            })
            .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid email or password")))
            .onErrorResume(UnauthorizedException.class, e -> Mono.error(e))
            .onErrorResume(e -> Mono.error(new UnauthorizedException("Authentication failed: " + e.getMessage())));
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
        return Mono.fromCallable(() -> {
            // Hash password before sending to Client-MS
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));

            // Set default role if not provided
            if (userDTO.getRole() == null || userDTO.getRole().isEmpty()) {
                userDTO.setRole("CLIENT");
            }
            return userDTO;
        })
        .flatMap(dto -> userServiceClient.createUser(dto))
        .map(createdUser -> {
            // Remove password from response
            createdUser.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        })
        .onErrorResume(e -> Mono.error(new RuntimeException("Registration failed: " + e.getMessage())));
    }
}
