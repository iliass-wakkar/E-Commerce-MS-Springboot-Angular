package com.Client.Service;

import com.Client.dto.*;
import com.Client.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDTO registerUser(UserCreateDTO createDTO);
    Optional<User> authenticate(String email, String rawPassword);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO);
    UserResponseDTO adminUpdateUser(Long id, AdminUserUpdateDTO updateDTO);
    void deleteUser(Long id);
}
