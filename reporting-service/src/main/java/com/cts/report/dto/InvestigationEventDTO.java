package com.cts.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvestigationEventDTO {

    private Long investigationId;
    private Long claimId;
    private String previousStatus;
    private String newStatus;
    private Long userId;
    private LocalDateTime changedAt;
}
