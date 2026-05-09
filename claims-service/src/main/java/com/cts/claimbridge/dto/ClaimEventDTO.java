package com.cts.claimbridge.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClaimEventDTO {
    private Long          claimId;
    private String        previousStatus;
    private String        newStatus;
    private String        lossType;
    private Double        estimatedAmount;
    private Long          userId;
    private LocalDateTime changedAt;
}
