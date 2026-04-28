package com.cts.identity.dto;

import lombok.Data;

@Data
public class PolicyHolderRequestDTO {
    private String name;
    private String contactInfo;
    private String businessType;
    private String taxID;
}
