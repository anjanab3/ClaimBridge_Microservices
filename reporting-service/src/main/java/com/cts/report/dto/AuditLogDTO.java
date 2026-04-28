package com.cts.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuditLogDTO {

    private Long logId;
    private Long userId;
    private String resource;
    private Long resourceId;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}
