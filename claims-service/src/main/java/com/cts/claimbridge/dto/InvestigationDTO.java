package com.cts.claimbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data

@NoArgsConstructor
public class InvestigationDTO { //A
    private Long investigationId;
    private Long assignedAdjusterId;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    public InvestigationDTO(Long investigationId, String status, LocalDateTime openedAt, LocalDateTime closedAt) {
        this.investigationId = investigationId;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
    }
}