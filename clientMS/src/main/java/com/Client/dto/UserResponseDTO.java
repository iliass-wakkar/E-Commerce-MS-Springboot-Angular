package com.Client.dto;

import com.Client.model.Role;
import java.time.Instant;

// Using Lombok annotations for boilerplate code
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String shippingAddress;
    private String phone;
    private Instant createdAt;
}
