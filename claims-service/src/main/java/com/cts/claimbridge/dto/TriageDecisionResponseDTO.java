package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.Priority;
import com.cts.claimbridge.util.TriageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class TriageDecisionResponseDTO { //A
    private Long decisionId;
    private Long claimId;
    private Long ruleId;
    private Priority priority;        // HIGH, MEDIUM, LOW, CRITICAL
    private String assignedQueue;   // ADJUSTER or FRAUD
    private String assignedTo;      // role_code of assigned user e.g. CA-001
    private TriageStatus status;          // OPEN, IN_REVIEW, CLOSED
    private LocalDateTime assignedAt;
    private String message;

}