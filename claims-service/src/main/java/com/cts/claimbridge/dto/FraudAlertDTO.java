package com.cts.claimbridge.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FraudAlertDTO { //A
    private Long alertId;
    private Long scoreId;
    private String reason;
    private String escalatedTo;
    private LocalDateTime escalatedAt;
    private String status;
}
