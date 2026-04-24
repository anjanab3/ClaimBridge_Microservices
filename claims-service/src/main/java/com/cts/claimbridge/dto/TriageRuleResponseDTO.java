package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.Priority;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TriageRuleResponseDTO {
    private Long ruleId;
    private String name;
    private String conditionsJSON;
    private Priority priority;        // HIGH, MEDIUM, LOW, CRITICAL
    private String assignedQueue;   // AUTO, PROPERTY, FRAUD
    private Boolean active;
    private Boolean isDefault;
    private String message;
}