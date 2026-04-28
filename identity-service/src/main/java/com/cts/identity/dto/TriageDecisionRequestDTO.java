package com.cts.identity.dto;

import com.cts.identity.util.TriageStatus;
import lombok.Data;

@Data
public class TriageDecisionRequestDTO {
    private Long claimId;
    private Long ruleId;
    private String assignedTo;   // userId of assignee e.g. CA-0001
    private TriageStatus status;
}
