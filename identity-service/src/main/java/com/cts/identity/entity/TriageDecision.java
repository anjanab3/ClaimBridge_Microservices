package com.cts.identity.entity;

import com.cts.identity.util.Priority;
import com.cts.identity.util.TriageStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "triage_decisions")
@Data
public class TriageDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    // Plain FK — Claim lives in claims-service (different DB)
    private Long claimId;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private String assignedQueue;
    private String assignedTo;       // userId of the assigned user (e.g. CA-0001)

    @Enumerated(EnumType.STRING)
    private TriageStatus status;

    private LocalDateTime assignedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "rule_id")
    @JsonBackReference(value = "rule-decisions")
    private TriageRule triageRule;
}
