package com.cts.report.dto;

import lombok.Data;

@Data
public class AuditEventDTO {
    private Long   userId;
    private String resource;
    private Long   resourceId;
    private String action;
    private String details;
}
