package com.cts.claimbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class FraudScoringResultDTO {

    private Long scoreId;
    private Long claimId;
    private int totalScore;

    // GREEN (0-40) | YELLOW (50-80) | RED (90+)
    private String riskBand;

    private List<Map<String, Object>> triggeredRules;
    private LocalDateTime calculatedAt;

    // true when score >= 90 and a FraudAlert + FRAUD triage decision were auto-created
    private boolean autoEscalated;
}
