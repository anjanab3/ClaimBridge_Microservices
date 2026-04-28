package com.cts.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SLABreachDTO {

    private String slaName;
    private String monitoredEntity;
    private String breachType;      // RESPONSE, RESOLUTION
    private Long entityId;
    private Long claimId;
    private long elapsedHours;
    private int limitHours;
    private long exceededBy;
}
