package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.PolicyStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

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

    @OneToMany(mappedBy = "policy",cascade = CascadeType.ALL)//A
    @JsonBackReference(value = "policy")//A
    private List<Claim> claim;//A
    
    @ManyToOne//A
    @JoinColumn(name = "holder_id")//A
    @JsonBackReference(value = "holder")//A
    private PolicyHolder holder;//A

}
