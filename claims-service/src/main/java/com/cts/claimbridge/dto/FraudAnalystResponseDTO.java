package com.cts.claimbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FraudAnalystResponseDTO {
    
    private Long alertId;
    private Long claimId;
    private String status;      // OPEN, ESCALATED, RESOLVED
    private String reason;      // Reason for the current status (e.g., "High score", "Manual review", "Escalated to senior analyst")
    private String message;     // Optional field to provide additional context or feedback to the analyst

}
