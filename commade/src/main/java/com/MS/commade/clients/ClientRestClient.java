package com.MS.commade.clients;

import com.MS.commade.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-client")
public interface ClientRestClient {

    @GetMapping("/api/v1/users/{id}")
    @CircuitBreaker(name = "clientService", fallbackMethod = "fallbackGetUserById")
    UserDto getUserById(@PathVariable("id") Long id);

    default UserDto fallbackGetUserById(Long id, Exception e) {
        // If Client Service is down, we cannot verify the user.
        // Throwing exception to prevent anonymous orders or security bypass.
        throw new RuntimeException("Client Service is unavailable. Cannot verify user " + id);
    }
}
