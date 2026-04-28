package com.cts.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String status;
}
