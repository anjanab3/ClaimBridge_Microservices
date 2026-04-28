package com.cts.identity.entity;

import com.cts.identity.util.PolicyStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "policies")
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
    private PolicyStatus status;

    @ManyToOne
    @JoinColumn(name = "holder_id")
    @JsonBackReference(value = "holder-policies")
    private PolicyHolder holder;
}
