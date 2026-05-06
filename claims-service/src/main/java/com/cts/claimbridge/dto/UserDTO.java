package com.cts.claimbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors identity-service's UserDTO — used for Feign responses from
 * /api/internal/users/* so claims-service never touches a local user table.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String status;
    private Long holderId;
}
