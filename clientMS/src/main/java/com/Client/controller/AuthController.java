package com.Client.controller;

import com.Client.dto.UserCreateDTO;
import com.Client.dto.UserResponseDTO;
import com.Client.dto.UserUpdateDTO;
import com.Client.model.User;
import com.Client.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody UserCreateDTO createDTO) {
        UserResponseDTO registeredUser = userService.registerUser(createDTO);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Get user by email (for authentication)
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> user = userService.getUserByEmail(loginRequest.getEmail());
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * Get current user information (mock implementation)
     * GET /auth/me
     *
     * Note: This is a simplified version. In a real application, you would:
     * 1. Extract user info from JWT token
     * 2. Get user ID from security context
     * 3. Return the authenticated user's details
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestParam(required = false) String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            // Convert to UserResponseDTO
            User foundUser = user.get();
            UserResponseDTO response = new UserResponseDTO(
                foundUser.getId(),
                foundUser.getFirstName(),
                foundUser.getLastName(),
                foundUser.getEmail(),
                foundUser.getRole(),
                foundUser.getShippingAddress(),
                foundUser.getPhone(),
                foundUser.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }

    /**
     * Update current user profile
     * PUT /auth/me
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            @RequestParam(required = false) String email,
            @RequestBody UserUpdateDTO updateDTO) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UserResponseDTO updatedUser = userService.updateUserByEmail(email, updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete current user account
     * DELETE /auth/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@RequestParam(required = false) String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        userService.deleteUserByEmail(email);
        return ResponseEntity.noContent().build();
    }

    // Inner class for login request
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

