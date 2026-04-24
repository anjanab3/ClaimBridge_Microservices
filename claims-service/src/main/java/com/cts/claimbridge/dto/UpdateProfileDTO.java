package com.cts.claimbridge.dto;


import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDTO {
    private String userName;
    private String email;
    private String phone;
}
