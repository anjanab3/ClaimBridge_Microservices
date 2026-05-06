package com.cts.claimbridge.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    @ManyToOne //A
    @JoinColumn(name = "claimId") //A
    @JsonBackReference(value = "alert") //A
    private Claim claim; //A
    private String reason;
    private String assignedTo;          // e.g. FA-0001
    private String escalatedTo;

    private LocalDateTime escalatedAt;
    private String status;              // OPEN, ESCALATED, RESOLVED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "fraudAlert")
    @JsonManagedReference(value = "alert-score")
    private FraudScore fraudScore;

    public Long getClaimId() {
        return claim != null ? claim.getClaimId() : null;
    }

    /** Flat numeric score for API consumers — avoids serialising the full FraudScore object. */
    public Double getFraudScoreValue() {
        return fraudScore != null ? fraudScore.getScoreValue() : null;
    }

}