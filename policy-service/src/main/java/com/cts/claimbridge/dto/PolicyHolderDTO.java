package com.cts.claimbridge.dto;

import lombok.Data;

@Data
public class PolicyHolderDTO { //A
    private Long holderID;
    private String name;
    private String contactInfo;
    private String businessType;
    private String taxID;
}