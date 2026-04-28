package com.cts.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class ClaimTrendResponseDTO {

    private int totalClaims;
    private Map<String, Long> byLossType;   // { "AUTO": 12, "PROPERTY": 8 }
    private Map<String, Long> byStatus;     // { "IN_COMING": 5, "SETTLED": 10 }
    private Double totalEstimatedAmount;
}
