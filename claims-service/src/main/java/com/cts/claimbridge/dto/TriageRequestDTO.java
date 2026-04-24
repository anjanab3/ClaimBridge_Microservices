package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.TriageStatus;
import lombok.Data;

@Data
public class TriageRequestDTO {
    private TriageStatus status;
    private String priority;
    private String assignedTo;
}