package com.cts.payment.dto;

import lombok.Data;

@Data
public class AuditEventDTO {
    private Long   userId;      // null for staff-only events
    private String resource;
    private Long   resourceId;
    private String action;
    private String details;
}
