package com.cts.claimbridge.dto;

import lombok.Data;

@Data
public class PolicyResponse {
    private Long policyId;
    private String policyNumber;
    private String policyType;
    private Double coverageAmount;
    private String status;
    private Long holderId;
    // add any other fields your policy-service returns
}