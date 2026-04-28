package com.cts.identity.dto;

import com.cts.identity.util.Priority;
import com.cts.identity.util.TriageStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TriageDecisionResponseDTO {
    private Long decisionId;
    private Long claimId;
    private Long ruleId;
    private Priority priority;
    private String assignedQueue;
    private String assignedTo;
    private TriageStatus status;
    private LocalDateTime assignedAt;
    private String message;
}
