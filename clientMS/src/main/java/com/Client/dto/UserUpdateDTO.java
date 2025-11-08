package com.Client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    private String shippingAddress;
    private String phone;
}
