package com.cts.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolicyHolderResponseDTO {
    private Long holderId;
    private String name;
    private String contactInfo;
    private String businessType;
    private String taxID;
}
