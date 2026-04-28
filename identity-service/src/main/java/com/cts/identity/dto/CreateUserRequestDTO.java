package com.cts.identity.dto;

import com.cts.identity.util.Role;
import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String username;
    private String email;
    private String phone;
    private String password;
    private Role role;
    private Long holderId;   // optional — link to a PolicyHolder
}
