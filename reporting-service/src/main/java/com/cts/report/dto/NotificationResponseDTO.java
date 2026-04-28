package com.cts.report.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponseDTO {

    private Long notificationId;
    private Long userId;
    private Long claimId;
    private String message;
    private String category;    // INTAKE, INVESTIGATION, PAYMENT, FRAUD
    private String status;      // UNREAD, READ
    private LocalDateTime createdAt;
}
