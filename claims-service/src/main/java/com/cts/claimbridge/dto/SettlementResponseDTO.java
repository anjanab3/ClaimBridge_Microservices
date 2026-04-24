package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SettlementResponseDTO { //A
    private Long settlementId;
    private Long claimId;
    private Double recommendedAmount;
    private String recommendedBy;
    private LocalDateTime recommendedAt;
    private String status;
}