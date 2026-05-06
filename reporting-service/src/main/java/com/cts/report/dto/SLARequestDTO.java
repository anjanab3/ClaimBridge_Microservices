package com.cts.report.dto;

import lombok.Data;

@Data
public class SLARequestDTO {

    private String name;
    private Integer responseHours;      // Max hours before first action must be taken
    private Integer resolutionHours;    // Max total hours to reach a terminal state
    private String monitoredEntity;     // CLAIM, INVESTIGATION, FRAUD_ALERT, SETTLEMENT
    private Boolean active;
}
