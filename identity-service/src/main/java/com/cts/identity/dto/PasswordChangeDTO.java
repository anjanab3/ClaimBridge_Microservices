package com.cts.identity.dto;

import lombok.Data;

@Data
public class PasswordChangeDTO {
    private String username;
    private String oldPassword;
    private String newPassword;
}
