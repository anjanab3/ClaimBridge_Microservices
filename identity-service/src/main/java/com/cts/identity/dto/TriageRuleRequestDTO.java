package com.cts.identity.dto;

import com.cts.identity.util.Priority;
import lombok.Data;

@Data
public class TriageRuleRequestDTO {
    private String name;
    private String conditionsJSON;
    private Priority priority;
    private String assignedQueue;
    private Boolean active;
    private Boolean isDefault;
}
