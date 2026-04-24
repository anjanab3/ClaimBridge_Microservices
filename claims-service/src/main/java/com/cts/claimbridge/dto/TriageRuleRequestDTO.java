package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.Priority;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriageRuleRequestDTO {

    private String name;
    private String conditionsJSON;  // e.g., {"lossType":"Auto","amount":">10000"}
    private Priority priority;        // HIGH, MEDIUM, LOW, CRITICAL
    private String assignedQueue;   // e.g., "AUTO", "PROPERTY", "FRAUD"
    private Boolean active;
    private Boolean isDefault;

}