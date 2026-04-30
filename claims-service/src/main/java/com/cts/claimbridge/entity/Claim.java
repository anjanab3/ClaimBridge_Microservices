package com.cts.claimbridge.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.cts.claimbridge.util.ClaimStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "claims")
@Getter
@Setter
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    @Column(nullable = false)
    private String reportedBy;

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String lossType;
    private Double estimatedAmount;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    private Long policyId;
//     @ManyToOne
// @JoinColumn(name = "policy_id")
// @JsonIgnore
// private Policy policy;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "inv")
    private Investigation investigation;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "claim")
    private List<Evidence> evidenceList;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "triage")
    private List<TriageDecision> triageDecisionList;

    @OneToOne(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "score")
    private FraudScore fraudScore;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "alert")
    private List<FraudAlert> alerts;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "set")
    private List<Settlement> settlementList;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    @JsonBackReference(value = "comm")
    private List<Communication> communicationList;

    @OneToMany(mappedBy = "claim")
    @JsonBackReference(value = "notification")
    private List<Notification> notificationList;
}