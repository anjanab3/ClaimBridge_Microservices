package com.cts.identity.dto;

import com.cts.identity.util.Priority;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TriageRuleResponseDTO {
    private Long ruleId;
    private String name;
    private String conditionsJSON;
    private Priority priority;
    private String assignedQueue;
    private Boolean active;
    private Boolean isDefault;
    private String message;
}
