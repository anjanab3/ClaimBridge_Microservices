package com.cts.identity.entity;

import com.cts.identity.util.Priority;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "triage_rules")
@Data
public class TriageRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String conditionsJSON;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private String assignedQueue;
    private Boolean active;
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "triageRule", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "rule-decisions")
    private List<TriageDecision> decisions;
}
