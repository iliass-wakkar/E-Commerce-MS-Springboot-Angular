package com.Client.Service;

import com.Client.dto.UserCreateDTO;
import com.Client.dto.UserResponseDTO;
import com.Client.dto.UserUpdateDTO;
import com.Client.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDTO registerUser(UserCreateDTO createDTO);

    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO);
    UserResponseDTO updateUserByEmail(String email, UserUpdateDTO updateDTO);
    void deleteUser(Long id);
    void deleteUserByEmail(String email);
}
