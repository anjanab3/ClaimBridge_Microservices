package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.PolicyStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @Column(nullable = false, unique = true)
    private String policyNumber;

    @Column(nullable = false)
    private String insuredName;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    @Column(columnDefinition = "TEXT")
    private String coverageJSON;

    @Enumerated(EnumType.STRING)
    private PolicyStatus status;  // ACTIVE, EXPIRED, SUSPENDED, CANCELLED

    private Long holderId;  // FK → policyholders.holderId
}
