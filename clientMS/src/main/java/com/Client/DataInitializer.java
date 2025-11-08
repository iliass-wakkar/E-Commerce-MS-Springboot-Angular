package com.Client;

import com.Client.dto.UserCreateDTO;
import com.Client.model.Role;
import com.Client.model.User;
import com.Client.repository.UserRepository;
import com.Client.Service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;

    public DataInitializer(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the admin user already exists
        if (userRepository.findByEmail("admin@ecommerce.com").isEmpty()) {
            // Use a DTO to create the user, but the service will set the role to CLIENT by default
            UserCreateDTO adminDTO = new UserCreateDTO();
            adminDTO.setFirstName("Admin");
            adminDTO.setLastName("User");
            adminDTO.setEmail("admin@ecommerce.com");
            adminDTO.setPassword("adminpassword"); // Use a secure password in a real app
            adminDTO.setShippingAddress("N/A");
            adminDTO.setPhone("N/A");

            // Register the user first
            userService.registerUser(adminDTO);

            // Now, fetch the user and update its role to ADMIN
            User adminUser = userRepository.findByEmail("admin@ecommerce.com").get();
            adminUser.setRole(Role.ADMIN);
            userRepository.save(adminUser);
        }
    }
}
