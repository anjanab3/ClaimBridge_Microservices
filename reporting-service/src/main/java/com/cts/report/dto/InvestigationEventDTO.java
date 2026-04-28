package com.cts.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InvestigationEventDTO {

    private Long investigationId;
    private Long claimId;
    private String previousStatus;
    private String newStatus;
    private Long userId;
    private LocalDateTime changedAt;
}
