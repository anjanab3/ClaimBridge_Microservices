package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClaimDTO { //A
    private Long claimId;
    private Long policyId;
    private String reportedBy;
    private LocalDate incidentDate;
    private String lossType;
    private Double estimatedAmount;
    private String status;
    private LocalDateTime createdAt;

}