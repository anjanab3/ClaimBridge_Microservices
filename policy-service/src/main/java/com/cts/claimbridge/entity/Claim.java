package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.ClaimStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

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
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime createdDate = LocalDateTime.now();

    private Long policyId;

    // @ManyToOne //A
    // @JoinColumn(name = "policy_id") //A
    // @JsonBackReference(value = "policy") //A
    // private Policy policy; //A

}