package com.Gateway.Server.client;

import com.Gateway.Server.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Feign client for calling MS-CLIENT service via Eureka
// Eureka will discover the service and resolve its location automatically
@FeignClient(name = "MS-CLIENT", contextId = "userServiceClient", path = "/api/v1")
public interface UserServiceClient {

    @GetMapping("/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable("email") String email);

    @PostMapping("/users")
    UserDTO createUser(@RequestBody UserDTO userDTO);
}

