package com.cts.claimbridge.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.cts.claimbridge.util.Priority;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class TriageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleID;

    @Column(nullable = false)
    private String name;

    // Store conditions as JSON string (e.g., {"lossType":"Auto","amount":">10000"})
    @Column(columnDefinition = "TEXT")
    private String conditionsJSON;

    @Enumerated(EnumType.STRING)
    private Priority priority;       // e.g., HIGH, MEDIUM, LOW
    
    private String assignedQueue;  // e.g., "Auto", "Property", "Fraud"
    private Boolean active;
    private Boolean isDefault = false;  // fallback rule when no other rule matches a claim

    @OneToMany(mappedBy = "triageRule", cascade = CascadeType.ALL)
    @JsonBackReference(value = "rule")
    private  List<TriageDecision> decisionList;

}
