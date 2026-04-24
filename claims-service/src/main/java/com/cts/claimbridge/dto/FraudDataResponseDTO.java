package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FraudDataResponseDTO { //A

    private Long scoreId;
    private Long claimId;
    private Double scoreValue;
    private String factorsJSON;
    private LocalDateTime calculatedAt;
    private List<FraudAlertDTO> alerts;

}