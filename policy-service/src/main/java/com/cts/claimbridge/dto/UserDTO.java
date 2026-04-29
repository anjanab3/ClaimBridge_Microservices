package com.cts.claimbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {

    private int userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String rolecode;
    private Long holderId;
}