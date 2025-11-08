package com.Client.dto;

import com.Client.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminUserUpdateDTO {
    private String firstName;
    private String lastName;
    private String shippingAddress;
    private String phone;
    private Role role;
}
