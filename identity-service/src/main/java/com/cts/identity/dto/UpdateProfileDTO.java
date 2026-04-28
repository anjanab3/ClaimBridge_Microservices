package com.cts.identity.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String email;
    private String phone;
    private String status;
}
