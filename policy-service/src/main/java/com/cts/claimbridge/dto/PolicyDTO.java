package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PolicyDTO { //A
    private Long policyID;
    private String policyNumber;
    private String insuredName;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String coverageJSON;
    private String status;
}