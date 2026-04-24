package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class InvUpdateResponseDTO { //A
    private Long investigationId;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private SettlementResponseDTO settlement;
}
