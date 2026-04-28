package com.cts.report.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ClaimTrendResponseDTO {

    private int totalClaims;
    private Map<String, Long> byLossType;   // { "AUTO": 12, "PROPERTY": 8 }
    private Map<String, Long> byStatus;     // { "IN_COMING": 5, "SETTLED": 10 }
    private Double totalEstimatedAmount;
}
