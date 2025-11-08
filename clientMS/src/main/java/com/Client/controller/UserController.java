package com.Client.controller;

import com.Client.dto.UserCreateDTO;
import com.Client.dto.UserResponseDTO;
import com.Client.dto.UserUpdateDTO;
import com.Client.model.User;
import com.Client.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserCreateDTO createDTO) {
        UserResponseDTO registeredUser = userService.registerUser(createDTO);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyAccount(@RequestAttribute("authenticatedUser") User user) {
        // The user object is added to the request by the AuthInterceptor
        UserResponseDTO userResponse = userService.getUserById(user.getId());
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyAccount(@RequestAttribute("authenticatedUser") User user, @RequestBody UserUpdateDTO updateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(user.getId(), updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@RequestAttribute("authenticatedUser") User user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.noContent().build();
    }
}
