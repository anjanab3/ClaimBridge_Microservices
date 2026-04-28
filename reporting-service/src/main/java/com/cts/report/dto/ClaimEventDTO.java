package com.cts.report.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ClaimEventDTO {

    private Long claimId;
    private String previousStatus;
    private String newStatus;
    private String lossType;
    private Double estimatedAmount;
    private Long userId;
    private LocalDateTime changedAt;
}
