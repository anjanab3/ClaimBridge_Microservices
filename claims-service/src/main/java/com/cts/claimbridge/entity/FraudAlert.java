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
    private String escalatedTo;

    private LocalDateTime escalatedAt;
    private String status;              // OPEN, ESCALATED, RESOLVED

    @OneToOne(mappedBy = "fraudAlert")
    @JsonManagedReference(value = "alert-score")
    private FraudScore fraudScore;

    public Long getClaimId() {
        return claim != null ? claim.getClaimId() : null;
    }

}