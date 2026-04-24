package com.cts.claimbridge.dto;

import com.cts.claimbridge.util.ClaimStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
@Builder
public class ClaimTrendResponseDTO {

    private int totalClaims;
    private Map<String, Long> byLossType;    // e.g., { "AUTO": 12, "PROPERTY": 8 }
    private Map<ClaimStatus, Long> byStatus;      // e.g., { "SUBMITTED": 5, "APPROVED": 10 }
    private Double totalEstimatedAmount;

}
