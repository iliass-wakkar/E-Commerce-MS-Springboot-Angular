package com.Gateway.Server.service;

import com.Gateway.Server.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceClientReactive {

    private final WebClient.Builder webClientBuilder;

    public Mono<UserDTO> getUserByEmail(String email) {
        return webClientBuilder.build()
                .get()
                .uri("http://MS-CLIENT/api/v1/users/email/{email}", email)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<UserDTO> getUserById(Long id) {
        return webClientBuilder.build()
                .get()
                .uri("http://MS-CLIENT/api/v1/users/{id}", id)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<UserDTO> createUser(UserDTO userDTO) {
        return webClientBuilder.build()
                .post()
                .uri("http://MS-CLIENT/api/v1/users")
                .bodyValue(userDTO)
                .retrieve()
                .bodyToMono(UserDTO.class);
    }
}

