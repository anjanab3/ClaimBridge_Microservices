package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvestigateUpdateStatusDTO { //A
    private String status;
    private Double recommendedAmount;
    private String recommendedBy;
}
