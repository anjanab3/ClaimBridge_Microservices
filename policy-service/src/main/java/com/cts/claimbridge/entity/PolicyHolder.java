package com.cts.claimbridge.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "policyholders")
@Data
public class PolicyHolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holderId;

    @Column(nullable = false)
    private String name;

    private String contactInfo;
    private String businessType;
    private String taxID;

    // holderId is referenced by users.holderId in identity-service — that is the
    // single source of truth for the User ↔ PolicyHolder link.
    // policyId is referenced by policies.holderId in this service.
}
