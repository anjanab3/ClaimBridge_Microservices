package com.cts.report.dto;

import lombok.Data;

@Data
public class NotificationRequestDTO {

    private Long userId;
    private Long claimId;
    private String message;
    private String category;    // INTAKE, INVESTIGATION, PAYMENT, FRAUD
}
