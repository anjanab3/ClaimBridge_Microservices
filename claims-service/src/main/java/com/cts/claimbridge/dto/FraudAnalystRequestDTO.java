package com.cts.claimbridge.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FraudAnalystRequestDTO {
    
    private Long claimId;
    private String note;        // Optional note from the analyst when escalating or resolving
    private String decision;    // CLEAR, ESCALATE, REJECT, REASSIGN
    private String escalatedTo; // Optional: specify who to escalate to
    private String assignedTo;  // role_code of the specific adjuster to reassign to e.g. CA-001
    private String status;      // Alert status update: OPEN, IN_PROGRESS, ESCALATED, RESOLVED, FRAUD
    private String reason;      // Free-text reason describing the analyst's finding

}
