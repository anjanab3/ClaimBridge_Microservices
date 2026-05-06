package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.Priority;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TriageRuleDTO {
    private Long    ruleId;
    private String  name;
    private String  conditionsJSON;
    private Priority priority;
    private String  assignedQueue;
    private Boolean active;
    private Boolean isDefault;
}
