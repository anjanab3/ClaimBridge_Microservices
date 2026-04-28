package com.cts.report.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimEventDTO {

    private Long claimId;
    private String previousStatus;
    private String newStatus;
    private String lossType;
    private Double estimatedAmount;
    private Long userId;
    private LocalDateTime changedAt;
}
