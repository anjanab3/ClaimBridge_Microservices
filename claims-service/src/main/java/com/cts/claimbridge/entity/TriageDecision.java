package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.Priority;
import com.cts.claimbridge.util.TriageStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class TriageDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    private Long ruleId;

    @Enumerated(EnumType.STRING)
    private Priority priority; // e.g., HIGH, MEDIUM, LOW

    private String assignedQueue;
    private String assignedTo;

    @Enumerated(EnumType.STRING)
    private TriageStatus status;  // OPEN, IN_PROGRESS, ESCALATED, CLOSED

    private LocalDateTime assignedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "claim_id", insertable = false, updatable = false)
    @JsonBackReference(value = "triage")
    private Claim claim;

    @ManyToOne
    @JoinColumn(name = "ruleId", insertable = false, updatable = false)
    @JsonBackReference(value = "rule")
    private TriageRule triageRule;
}