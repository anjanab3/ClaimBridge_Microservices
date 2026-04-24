package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.TriageStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriageDecisionRequestDTO {
    private Long claimId;
    private Long ruleId;
    private String assignedTo;  // role_code of the user to assign e.g. CA-001 (optional)
    private TriageStatus status;      // for manual status update via PUT only
}
