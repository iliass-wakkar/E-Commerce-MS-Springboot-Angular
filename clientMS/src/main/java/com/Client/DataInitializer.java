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
            UserCreateDTO adminDTO = new UserCreateDTO();
            adminDTO.setFirstName("Admin");
            adminDTO.setLastName("User");
            adminDTO.setEmail("admin@ecommerce.com");
            adminDTO.setPassword("adminpassword");
            adminDTO.setShippingAddress("N/A");
            adminDTO.setPhone("N/A");

            userService.registerUser(adminDTO);

            User adminUser = userRepository.findByEmail("admin@ecommerce.com").get();
            adminUser.setRole(Role.ADMIN);
            userRepository.save(adminUser);
        }

        // Add client 1
        if (userRepository.findByEmail("client1@ecommerce.com").isEmpty()) {
            UserCreateDTO client1DTO = new UserCreateDTO();
            client1DTO.setFirstName("John");
            client1DTO.setLastName("Doe");
            client1DTO.setEmail("client1@ecommerce.com");
            client1DTO.setPassword("password123");
            client1DTO.setShippingAddress("123 Main St, City, Country");
            client1DTO.setPhone("+1234567890");
            userService.registerUser(client1DTO);
        }

        // Add client 2
        if (userRepository.findByEmail("client2@ecommerce.com").isEmpty()) {
            UserCreateDTO client2DTO = new UserCreateDTO();
            client2DTO.setFirstName("Jane");
            client2DTO.setLastName("Smith");
            client2DTO.setEmail("client2@ecommerce.com");
            client2DTO.setPassword("password123");
            client2DTO.setShippingAddress("456 Oak Ave, City, Country");
            client2DTO.setPhone("+9876543210");
            userService.registerUser(client2DTO);
        }

        // Add client 3
        if (userRepository.findByEmail("client3@ecommerce.com").isEmpty()) {
            UserCreateDTO client3DTO = new UserCreateDTO();
            client3DTO.setFirstName("Michael");
            client3DTO.setLastName("Johnson");
            client3DTO.setEmail("client3@ecommerce.com");
            client3DTO.setPassword("password123");
            client3DTO.setShippingAddress("789 Pine Rd, City, Country");
            client3DTO.setPhone("+1122334455");
            userService.registerUser(client3DTO);
        }
    }
}
